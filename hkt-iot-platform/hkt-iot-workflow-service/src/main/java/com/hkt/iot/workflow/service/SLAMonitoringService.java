package com.hkt.iot.workflow.service;

import com.hkt.iot.workflow.domain.model.entity.SLAConfig;
import com.hkt.iot.workflow.domain.model.entity.SLAMonitor;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.SLAConfigRepository;
import com.hkt.iot.workflow.domain.repository.SLAMonitorRepository;
import com.hkt.iot.workflow.infrastructure.messaging.DomainEventPublisher;
import com.hkt.iot.workflow.domain.model.domainevent.SLAMonitoringWarningEvent;
import com.hkt.iot.workflow.domain.model.domainevent.SLAMonitoringBreachedEvent;
import com.hkt.iot.workflow.domain.model.domainevent.SLAWarningEvent;
import com.hkt.iot.workflow.domain.model.domainevent.SLABreachEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SLA 监控服务
 * 负责 SLA 计时、预警和超时检查
 *
 * @author HKT IoT Team
 */
@Service
@Slf4j
public class SLAMonitoringService {

    private final SLAConfigRepository slaConfigRepository;
    private final SLAMonitorRepository slaMonitorRepository;
    private final DomainEventPublisher eventPublisher;

    private static final Duration DEFAULT_WARNING_THRESHOLD = Duration.ofMinutes(30);
    private static final Duration CRITICAL_THRESHOLD = Duration.ofMinutes(10);

    public SLAMonitoringService(
            SLAConfigRepository slaConfigRepository,
            SLAMonitorRepository slaMonitorRepository,
            DomainEventPublisher eventPublisher) {
        this.slaConfigRepository = slaConfigRepository;
        this.slaMonitorRepository = slaMonitorRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 启动 SLA 计时
     */
    @Transactional
    public void startSLAClock(
            String processInstanceId,
            String taskId,
            String slaConfigId,
            String tenantId) {

        SLAConfig config = slaConfigRepository.findById(slaConfigId)
                .orElseThrow(() -> new IllegalArgumentException("SLA 配置不存在：" + slaConfigId));

        LocalDateTime deadline = LocalDateTime.now()
                .plus(config.getResponseTimeLimit());

        SLAMonitor monitor = SLAMonitor.create(
                ProcessInstanceId.of(processInstanceId),
                taskId != null ? TaskId.of(taskId) : null,
                slaConfigId,
                deadline,
                TenantId.of(tenantId)
        );

        slaMonitorRepository.save(monitor);
        log.info("Started SLA clock: monitorId={}, processInstanceId={}, taskId={}, deadline={}",
                monitor.getId().getValue(), processInstanceId, taskId, deadline);
    }

    /**
     * 检查并发送 SLA 预警
     * 根据配置的预警阈值检查并发送预警通知
     *
     * @return 发送预警的数量
     */
    @Transactional
    public int checkAndSendWarnings() {
        LocalDateTime now = LocalDateTime.now();
        int warningCount = 0;

        List<SLAMonitor> pendingMonitors = slaMonitorRepository.findAll().stream()
                .filter(m -> m.getResponseStatus() == SLAStatus.PENDING || m.getResponseStatus() == SLAStatus.WARNING)
                .filter(m -> m.getWarningSentAt() == null)
                .toList();

        for (SLAMonitor monitor : pendingMonitors) {
            Duration warningThreshold = getWarningThreshold(monitor.getSlaConfigId());
            Duration remainingTime = monitor.calculateRemainingTime();

            if (remainingTime.isNegative() || remainingTime.isZero()) {
                continue;
            }

            SLAWarningLevel warningLevel = determineWarningLevel(remainingTime, warningThreshold);

            if (warningLevel != SLAWarningLevel.NORMAL) {
                monitor.updateWarningLevel(warningLevel);
                monitor.markResponseWarning();
                monitor.markWarningSent();
                slaMonitorRepository.save(monitor);

                eventPublisher.publish(new SLAWarningEvent(
                        monitor.getId(),
                        monitor.getProcessInstanceId(),
                        monitor.getTaskId(),
                        warningLevel,
                        remainingTime,
                        now
                ));

                warningCount++;
                log.warn("SLA warning sent: monitorId={}, level={}, remainingTime={} minutes",
                        monitor.getId().getValue(), warningLevel, remainingTime.toMinutes());
            }
        }

        log.info("SLA warning check completed: {} warnings sent", warningCount);
        return warningCount;
    }

    /**
     * 检查并记录 SLA 违规
     *
     * @return 记录违规的数量
     */
    @Transactional
    public int checkAndRecordBreaches() {
        LocalDateTime now = LocalDateTime.now();
        int breachCount = 0;

        List<SLAMonitor> breachedMonitors = slaMonitorRepository.findAll().stream()
                .filter(m -> m.getResponseStatus() != SLAStatus.BREACHED && m.getResponseStatus() != SLAStatus.COMPLIANT)
                .filter(m -> m.getBreachSentAt() == null)
                .filter(SLAMonitor::checkBreach)
                .toList();

        for (SLAMonitor monitor : breachedMonitors) {
            monitor.markResponseBreached();
            monitor.updateWarningLevel(SLAWarningLevel.BREACHED);
            monitor.markBreachSent();
            slaMonitorRepository.save(monitor);

            Duration breachDuration = monitor.calculateBreachDuration();

            eventPublisher.publish(new SLABreachEvent(
                    monitor.getId(),
                    monitor.getProcessInstanceId(),
                    monitor.getTaskId(),
                    SLABreachEvent.BreachType.RESPONSE,
                    breachDuration,
                    now
            ));

            breachCount++;
            log.error("SLA breach recorded: monitorId={}, breachDuration={} minutes",
                    monitor.getId().getValue(), breachDuration.toMinutes());
        }

        log.info("SLA breach check completed: {} breaches recorded", breachCount);
        return breachCount;
    }

    /**
     * 计算 SLA 统计数据
     *
     * @param tenantId 租户ID
     * @return SLA 统计数据
     */
    public SLAStatistics calculateStatistics(String tenantId) {
        List<SLAMonitor> monitors = slaMonitorRepository.findAll().stream()
                .filter(m -> tenantId == null || m.getTenantId().getValue().equals(tenantId))
                .toList();

        return calculateStatisticsFromMonitors(monitors);
    }

    /**
     * 计算 SLA 统计数据（按流程定义）
     *
     * @param processDefinitionKey 流程定义Key
     * @param tenantId             租户ID
     * @return SLA 统计数据
     */
    public SLAStatistics calculateStatistics(String processDefinitionKey, String tenantId) {
        List<SLAMonitor> monitors = slaMonitorRepository.findAll().stream()
                .filter(m -> tenantId == null || m.getTenantId().getValue().equals(tenantId))
                .toList();

        return calculateStatisticsFromMonitors(monitors);
    }

    /**
     * 从监控记录列表计算统计数据
     */
    private SLAStatistics calculateStatisticsFromMonitors(List<SLAMonitor> monitors) {
        long totalTasks = monitors.size();
        long completedTasks = monitors.stream()
                .filter(m -> m.getResponseStatus() == SLAStatus.COMPLIANT || m.getResponseStatus() == SLAStatus.BREACHED)
                .count();
        long breachedTasks = monitors.stream()
                .filter(m -> m.getResponseStatus() == SLAStatus.BREACHED)
                .count();

        Duration totalResponseTime = Duration.ZERO;
        Duration totalResolutionTime = Duration.ZERO;
        int responseCount = 0;
        int resolutionCount = 0;

        for (SLAMonitor monitor : monitors) {
            if (monitor.getActualResponseTime() != null && monitor.getCreatedAt() != null) {
                totalResponseTime = totalResponseTime.plus(
                        Duration.between(monitor.getCreatedAt(), monitor.getActualResponseTime()));
                responseCount++;
            }
            if (monitor.getActualResolutionTime() != null && monitor.getCreatedAt() != null) {
                totalResolutionTime = totalResolutionTime.plus(
                        Duration.between(monitor.getCreatedAt(), monitor.getActualResolutionTime()));
                resolutionCount++;
            }
        }

        Duration averageResponseTime = responseCount > 0
                ? totalResponseTime.dividedBy(responseCount)
                : Duration.ZERO;
        Duration averageResolutionTime = resolutionCount > 0
                ? totalResolutionTime.dividedBy(resolutionCount)
                : Duration.ZERO;

        double slaComplianceRate = completedTasks > 0
                ? ((double) (completedTasks - breachedTasks) / completedTasks) * 100
                : 100.0;

        return SLAStatistics.builder()
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .breachedTasks(breachedTasks)
                .averageResponseTime(averageResponseTime)
                .averageResolutionTime(averageResolutionTime)
                .slaComplianceRate(slaComplianceRate)
                .build();
    }

    /**
     * 获取预警阈值
     */
    private Duration getWarningThreshold(String slaConfigId) {
        if (slaConfigId == null) {
            return DEFAULT_WARNING_THRESHOLD;
        }
        return slaConfigRepository.findById(slaConfigId)
                .map(SLAConfig::getWarningThreshold)
                .orElse(DEFAULT_WARNING_THRESHOLD);
    }

    /**
     * 根据剩余时间确定预警级别
     */
    private SLAWarningLevel determineWarningLevel(Duration remainingTime, Duration warningThreshold) {
        if (remainingTime.isNegative() || remainingTime.isZero()) {
            return SLAWarningLevel.BREACHED;
        }

        if (remainingTime.compareTo(CRITICAL_THRESHOLD) <= 0) {
            return SLAWarningLevel.CRITICAL;
        }

        if (remainingTime.compareTo(warningThreshold) <= 0) {
            return SLAWarningLevel.WARNING;
        }

        return SLAWarningLevel.NORMAL;
    }

    /**
     * 标记 SLA 响应完成
     */
    @Transactional
    public void markResponseCompliant(String monitorId) {
        SLAMonitor monitor = slaMonitorRepository.findById(SLAMonitorId.of(monitorId))
                .orElseThrow(() -> new IllegalArgumentException("SLA 监控记录不存在：" + monitorId));
        monitor.markResponseCompliant();
        slaMonitorRepository.save(monitor);
        log.info("Marked SLA response compliant: monitorId={}", monitorId);
    }

    /**
     * 标记 SLA 解决完成
     */
    @Transactional
    public void markResolutionCompliant(String monitorId) {
        SLAMonitor monitor = slaMonitorRepository.findById(SLAMonitorId.of(monitorId))
                .orElseThrow(() -> new IllegalArgumentException("SLA 监控记录不存在：" + monitorId));
        monitor.markResolutionCompliant();
        slaMonitorRepository.save(monitor);
        log.info("Marked SLA resolution compliant: monitorId={}", monitorId);
    }
}
