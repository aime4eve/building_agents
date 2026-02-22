package com.hkt.iot.device.application.service;

import com.hkt.iot.device.domain.model.OTATask;
import com.hkt.iot.device.domain.model.Device;
import com.hkt.iot.device.domain.repository.OTATaskRepository;
import com.hkt.iot.device.domain.repository.DeviceRepository;
import com.hkt.iot.device.application.event.DeviceEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * OTA升级应用服务
 * 负责设备固件升级任务的管理与执行
 *
 * @author HKT IoT Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OTAService {

    private final OTATaskRepository otaTaskRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceEventPublisher eventPublisher;

    /**
     * 创建OTA升级任务
     */
    @Transactional(rollbackFor = Exception.class)
    public OTATask createTask(OTACreateRequest request, Long createdBy) {
        log.info("创建OTA任务: taskName={}, targetVersion={}",
                request.getTaskName(), request.getTargetVersion());

        // 检查任务编码是否已存在
        if (otaTaskRepository.existsByTenantIdAndTaskCode(
                request.getTenantId(), request.getTaskCode())) {
            throw new IllegalArgumentException("任务编码已存在: " + request.getTaskCode());
        }

        // 创建OTA任务
        OTATask task = OTATask.create(
                request.getTenantId(),
                request.getTaskName(),
                request.getTaskCode(),
                request.getTaskType(),
                request.getCurrentVersion(),
                request.getTargetVersion(),
                request.getFirmwareUrl(),
                request.getFirmwareSize(),
                request.getFirmwareMd5(),
                request.getUpgradeStrategy(),
                request.getScheduledTime(),
                request.getTotalDevices(),
                request.getRollbackEnabled(),
                request.getRollbackVersion(),
                request.getTimeoutSeconds(),
                request.getRetryTimes(),
                request.getDescription(),
                createdBy
        );

        OTATask savedTask = otaTaskRepository.save(task);

        // 发布领域事件
        eventPublisher.publishDomainEvents(task);

        log.info("OTA任务创建成功: taskId={}, taskCode={}",
                savedTask.getId(), savedTask.getTaskCode());
        return savedTask;
    }

    /**
     * 开始执行OTA任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void startTask(Long taskId) {
        log.info("开始执行OTA任务: taskId={}", taskId);

        OTATask task = otaTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        task.start();
        otaTaskRepository.save(task);

        log.info("OTA任务开始执行: taskId={}", taskId);
    }

    /**
     * 暂停OTA任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void pauseTask(Long taskId) {
        log.info("暂停OTA任务: taskId={}", taskId);

        OTATask task = otaTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        task.pause();
        otaTaskRepository.save(task);

        log.info("OTA任务已暂停: taskId={}", taskId);
    }

    /**
     * 恢复OTA任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void resumeTask(Long taskId) {
        log.info("恢复OTA任务: taskId={}", taskId);

        OTATask task = otaTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        task.resume();
        otaTaskRepository.save(task);

        log.info("OTA任务已恢复: taskId={}", taskId);
    }

    /**
     * 取消OTA任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long taskId, String reason) {
        log.info("取消OTA任务: taskId={}, reason={}", taskId, reason);

        OTATask task = otaTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        task.cancel(reason);
        otaTaskRepository.save(task);

        log.info("OTA任务已取消: taskId={}", taskId);
    }

    /**
     * 更新设备升级进度
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceProgress(Long taskId, boolean success, String errorMessage) {
        log.debug("更新设备升级进度: taskId={}, success={}", taskId, success);

        OTATask task = otaTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));

        task.updateProgress(success, errorMessage);
        otaTaskRepository.save(task);

        log.debug("设备升级进度更新完成: taskId={}, progress={}%",
                taskId, task.getProgressPercentage());
    }

    /**
     * 查询OTA任务详情
     */
    @Transactional(readOnly = true)
    public OTATask getTaskById(Long taskId) {
        return otaTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));
    }

    /**
     * 查询租户的所有OTA任务
     */
    @Transactional(readOnly = true)
    public List<OTATask> getTasksByTenantId(Long tenantId) {
        return otaTaskRepository.findByTenantId(tenantId);
    }

    /**
     * 查询待执行的任务（定时任务调用）
     */
    @Transactional(readOnly = true)
    public List<OTATask> getPendingTasks() {
        LocalDateTime now = LocalDateTime.now();
        return otaTaskRepository.findByTaskStatusAndScheduledTimeBefore(
                OTATask.TaskStatus.SCHEDULED, now);
    }

    /**
     * 处理超时任务（定时任务调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleTimeoutTasks() {
        log.debug("开始处理超时OTA任务...");

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusHours(24);
        List<OTATask> timeoutTasks = otaTaskRepository.findByTaskStatusAndStartedTimeBefore(
                OTATask.TaskStatus.RUNNING, timeoutThreshold);

        for (OTATask task : timeoutTasks) {
            try {
                task.fail("任务执行超时");
                otaTaskRepository.save(task);
                log.info("OTA任务已标记为超时失败: taskId={}", task.getId());
            } catch (Exception e) {
                log.error("处理超时任务失败: taskId={}, error={}",
                        task.getId(), e.getMessage(), e);
            }
        }

        log.debug("超时OTA任务处理完成，处理数量: {}", timeoutTasks.size());
    }

    /**
     * OTA任务创建请求
     */
    public static class OTACreateRequest {
        private final Long tenantId;
        private final String taskName;
        private final String taskCode;
        private final OTATask.TaskType taskType;
        private final String currentVersion;
        private final String targetVersion;
        private final String firmwareUrl;
        private final Long firmwareSize;
        private final String firmwareMd5;
        private final OTATask.UpgradeStrategy upgradeStrategy;
        private final LocalDateTime scheduledTime;
        private final Integer totalDevices;
        private final Boolean rollbackEnabled;
        private final String rollbackVersion;
        private final Integer timeoutSeconds;
        private final Integer retryTimes;
        private final String description;

        public OTACreateRequest(Long tenantId, String taskName, String taskCode,
                                OTATask.TaskType taskType, String currentVersion,
                                String targetVersion, String firmwareUrl,
                                Long firmwareSize, String firmwareMd5,
                                OTATask.UpgradeStrategy upgradeStrategy,
                                LocalDateTime scheduledTime, Integer totalDevices,
                                Boolean rollbackEnabled, String rollbackVersion,
                                Integer timeoutSeconds, Integer retryTimes,
                                String description) {
            this.tenantId = tenantId;
            this.taskName = taskName;
            this.taskCode = taskCode;
            this.taskType = taskType;
            this.currentVersion = currentVersion;
            this.targetVersion = targetVersion;
            this.firmwareUrl = firmwareUrl;
            this.firmwareSize = firmwareSize;
            this.firmwareMd5 = firmwareMd5;
            this.upgradeStrategy = upgradeStrategy;
            this.scheduledTime = scheduledTime;
            this.totalDevices = totalDevices;
            this.rollbackEnabled = rollbackEnabled;
            this.rollbackVersion = rollbackVersion;
            this.timeoutSeconds = timeoutSeconds;
            this.retryTimes = retryTimes;
            this.description = description;
        }

        public Long getTenantId() { return tenantId; }
        public String getTaskName() { return taskName; }
        public String getTaskCode() { return taskCode; }
        public OTATask.TaskType getTaskType() { return taskType; }
        public String getCurrentVersion() { return currentVersion; }
        public String getTargetVersion() { return targetVersion; }
        public String getFirmwareUrl() { return firmwareUrl; }
        public Long getFirmwareSize() { return firmwareSize; }
        public String getFirmwareMd5() { return firmwareMd5; }
        public OTATask.UpgradeStrategy getUpgradeStrategy() { return upgradeStrategy; }
        public LocalDateTime getScheduledTime() { return scheduledTime; }
        public Integer getTotalDevices() { return totalDevices; }
        public Boolean getRollbackEnabled() { return rollbackEnabled; }
        public String getRollbackVersion() { return rollbackVersion; }
        public Integer getTimeoutSeconds() { return timeoutSeconds; }
        public Integer getRetryTimes() { return retryTimes; }
        public String getDescription() { return description; }
    }
}
