package com.hkt.iot.device.domain.model;

import com.hkt.iot.domain.model.AggregateRoot;
import com.hkt.iot.device.domain.event.OTATaskCompletedEvent;
import com.hkt.iot.device.domain.event.OTATaskCreatedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

/**
 * OTA升级任务聚合根
 * 管理设备固件升级任务的生命周期
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "ota_task")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OTATask extends AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "task_name", nullable = false, length = 200)
    private String taskName;

    @Column(name = "task_code", nullable = false, length = 100, unique = true)
    private String taskCode;

    @Column(name = "task_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    @Column(name = "task_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;

    @Column(name = "firmware_version", length = 50)
    private String currentVersion;

    @Column(name = "target_version", nullable = false, length = 50)
    private String targetVersion;

    @Column(name = "firmware_url", nullable = false, length = 500)
    private String firmwareUrl;

    @Column(name = "firmware_size")
    private Long firmwareSize;

    @Column(name = "firmware_md5", length = 100)
    private String firmwareMd5;

    @Column(name = "firmware_sha256", length = 200)
    private String firmwareSha256;

    @Column(name = "upgrade_strategy", length = 20)
    @Enumerated(EnumType.STRING)
    private UpgradeStrategy upgradeStrategy;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "started_time")
    private LocalDateTime startedTime;

    @Column(name = "completed_time")
    private LocalDateTime completedTime;

    @Column(name = "total_devices", nullable = false)
    private Integer totalDevices;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "failure_count")
    private Integer failureCount;

    @Column(name = "pending_count")
    private Integer pendingCount;

    @Column(name = "progress_percentage")
    private Integer progressPercentage;

    @Column(name = "rollback_enabled", nullable = false)
    private Boolean rollbackEnabled;

    @Column(name = "rollback_version", length = 50)
    private String rollbackVersion;

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    @Column(name = "retry_times")
    private Integer retryTimes;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * 任务类型
     */
    public enum TaskType {
        FIRMWARE_UPGRADE,    // 固件升级
        FIRMWARE_DOWNGRADE,  // 固件降级
        BATCH_UPGRADE        // 批量升级
    }

    /**
     * 任务状态
     */
    public enum TaskStatus {
        PENDING,      // 待执行
        SCHEDULED,    // 已调度
        RUNNING,      // 执行中
        PAUSED,       // 已暂停
        COMPLETED,    // 已完成
        FAILED,       // 失败
        CANCELLED     // 已取消
    }

    /**
     * 升级策略
     */
    public enum UpgradeStrategy {
        IMMEDIATE,    // 立即升级
        SCHEDULED,    // 定时升级
        GRADUAL,      // 灰度升级
        MANUAL        // 手动升级
    }

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    /**
     * 工厂方法：创建OTA升级任务
     */
    public static OTATask create(
            Long tenantId,
            String taskName,
            String taskCode,
            TaskType taskType,
            String currentVersion,
            String targetVersion,
            String firmwareUrl,
            Long firmwareSize,
            String firmwareMd5,
            UpgradeStrategy upgradeStrategy,
            LocalDateTime scheduledTime,
            Integer totalDevices,
            Boolean rollbackEnabled,
            String rollbackVersion,
            Integer timeoutSeconds,
            Integer retryTimes,
            String description,
            Long createdBy) {

        OTATask task = new OTATask();
        task.tenantId = tenantId;
        task.taskName = taskName;
        task.taskCode = taskCode;
        task.taskType = taskType;
        task.taskStatus = TaskStatus.PENDING;
        task.currentVersion = currentVersion;
        task.targetVersion = targetVersion;
        task.firmwareUrl = firmwareUrl;
        task.firmwareSize = firmwareSize;
        task.firmwareMd5 = firmwareMd5;
        task.upgradeStrategy = upgradeStrategy;
        task.scheduledTime = scheduledTime;
        task.totalDevices = totalDevices;
        task.pendingCount = totalDevices;
        task.successCount = 0;
        task.failureCount = 0;
        task.progressPercentage = 0;
        task.rollbackEnabled = rollbackEnabled;
        task.rollbackVersion = rollbackVersion;
        task.timeoutSeconds = timeoutSeconds != null ? timeoutSeconds : 1800; // 默认30分钟
        task.retryTimes = retryTimes != null ? retryTimes : 3;
        task.description = description;
        task.deleted = false;
        task.createdAt = LocalDateTime.now();
        task.updatedAt = LocalDateTime.now();
        task.createdBy = createdBy;
        task.updatedBy = createdBy;
        task.version = 0L;

        // 发布OTA任务创建事件
        task.registerDomainEvent(new OTATaskCreatedEvent(
                task.id,
                task.taskCode,
                task.tenantId,
                task.taskType,
                task.targetVersion,
                task.createdAt
        ));

        return task;
    }

    /**
     * 开始执行任务
     */
    public void start() {
        if (this.taskStatus != TaskStatus.PENDING && this.taskStatus != TaskStatus.SCHEDULED) {
            throw new IllegalStateException("只有待执行或已调度的任务才能开始");
        }
        this.taskStatus = TaskStatus.RUNNING;
        this.startedTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 暂停任务
     */
    public void pause() {
        if (this.taskStatus != TaskStatus.RUNNING) {
            throw new IllegalStateException("只有执行中的任务才能暂停");
        }
        this.taskStatus = TaskStatus.PAUSED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 恢复任务
     */
    public void resume() {
        if (this.taskStatus != TaskStatus.PAUSED) {
            throw new IllegalStateException("只有已暂停的任务才能恢复");
        }
        this.taskStatus = TaskStatus.RUNNING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 取消任务
     */
    public void cancel(String reason) {
        if (this.taskStatus == TaskStatus.COMPLETED || this.taskStatus == TaskStatus.FAILED) {
            throw new IllegalStateException("已完成或失败的任务不能取消");
        }
        this.taskStatus = TaskStatus.CANCELLED;
        this.errorMessage = reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新设备升级进度
     */
    public void updateProgress(boolean success, String errorMessage) {
        if (success) {
            this.successCount++;
        } else {
            this.failureCount++;
            this.errorMessage = errorMessage;
        }
        this.pendingCount--;

        // 计算进度百分比
        int completed = this.successCount + this.failureCount;
        this.progressPercentage = (completed * 100) / this.totalDevices;

        this.updatedAt = LocalDateTime.now();

        // 检查是否全部完成
        if (this.pendingCount <= 0) {
            complete();
        }
    }

    /**
     * 完成任务
     */
    private void complete() {
        if (this.failureCount > 0 && this.successCount == 0) {
            this.taskStatus = TaskStatus.FAILED;
        } else {
            this.taskStatus = TaskStatus.COMPLETED;
        }
        this.completedTime = LocalDateTime.now();

        // 发布OTA任务完成事件
        registerDomainEvent(new OTATaskCompletedEvent(
                this.id,
                this.taskCode,
                this.tenantId,
                this.taskStatus,
                this.successCount,
                this.failureCount,
                this.completedTime
        ));
    }

    /**
     * 任务失败
     */
    public void fail(String errorMessage) {
        this.taskStatus = TaskStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new OTATaskCompletedEvent(
                this.id,
                this.taskCode,
                this.tenantId,
                this.taskStatus,
                this.successCount,
                this.failureCount,
                this.completedTime
        ));
    }

    /**
     * 软删除
     */
    public void softDelete(Long deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查任务是否可以执行
     */
    public boolean canExecute() {
        return taskStatus == TaskStatus.PENDING || taskStatus == TaskStatus.SCHEDULED;
    }

    /**
     * 检查任务是否已完成
     */
    public boolean isCompleted() {
        return taskStatus == TaskStatus.COMPLETED || taskStatus == TaskStatus.FAILED || taskStatus == TaskStatus.CANCELLED;
    }
}
