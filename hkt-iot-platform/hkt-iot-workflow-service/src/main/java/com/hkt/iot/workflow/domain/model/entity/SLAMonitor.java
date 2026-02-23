package com.hkt.iot.workflow.domain.model.entity;

import com.hkt.iot.domain.model.Entity;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * SLA 监控记录实体
 *
 * @author HKT IoT Team
 */
@Getter
@NoArgsConstructor
public class SLAMonitor extends Entity<String> {

    private SLAMonitorId id;
    private ProcessInstanceId processInstanceId;
    private TaskId taskId;
    private String slaConfigId;
    private LocalDateTime slaDeadline;
    private SLAStatus responseStatus;
    private SLAStatus resolutionStatus;
    private LocalDateTime actualResponseTime;
    private LocalDateTime actualResolutionTime;
    private SLAWarningLevel warningLevel;
    private LocalDateTime warningSentAt;
    private LocalDateTime breachSentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private TenantId tenantId;

    /**
     * 工厂方法：创建 SLA 监控记录
     */
    public static SLAMonitor create(
            ProcessInstanceId processInstanceId,
            TaskId taskId,
            String slaConfigId,
            LocalDateTime slaDeadline,
            TenantId tenantId) {
        SLAMonitor monitor = new SLAMonitor();
        monitor.id = SLAMonitorId.generate();
        monitor.processInstanceId = processInstanceId;
        monitor.taskId = taskId;
        monitor.slaConfigId = slaConfigId;
        monitor.slaDeadline = slaDeadline;
        monitor.responseStatus = SLAStatus.PENDING;
        monitor.resolutionStatus = SLAStatus.PENDING;
        monitor.warningLevel = SLAWarningLevel.NORMAL;
        monitor.tenantId = tenantId;
        monitor.createdAt = LocalDateTime.now();
        monitor.updatedAt = LocalDateTime.now();
        return monitor;
    }

    /**
     * 检查是否需要预警
     *
     * @param warningThreshold 预警阈值
     * @return 是否需要预警
     */
    public boolean checkWarning(Duration warningThreshold) {
        if (responseStatus != SLAStatus.PENDING && responseStatus != SLAStatus.WARNING) {
            return false;
        }
        if (slaDeadline == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warningTime = slaDeadline.minus(warningThreshold);
        return now.isAfter(warningTime) && now.isBefore(slaDeadline);
    }

    /**
     * 检查是否已违规
     *
     * @return 是否已违规
     */
    public boolean checkBreach() {
        if (responseStatus == SLAStatus.BREACHED || responseStatus == SLAStatus.COMPLIANT) {
            return false;
        }
        if (slaDeadline == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(slaDeadline);
    }

    /**
     * 计算剩余时间
     *
     * @return 剩余时间，如果已超时返回负值
     */
    public Duration calculateRemainingTime() {
        if (slaDeadline == null) {
            return Duration.ZERO;
        }
        return Duration.between(LocalDateTime.now(), slaDeadline);
    }

    /**
     * 计算违规时长
     *
     * @return 违规时长，如果未违规返回零
     */
    public Duration calculateBreachDuration() {
        if (slaDeadline == null || !checkBreach()) {
            return Duration.ZERO;
        }
        return Duration.between(slaDeadline, LocalDateTime.now());
    }

    /**
     * 标记预警已发送
     */
    public void markWarningSent() {
        this.warningSentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 标记违规通知已发送
     */
    public void markBreachSent() {
        this.breachSentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新预警级别
     *
     * @param newLevel 新的预警级别
     */
    public void updateWarningLevel(SLAWarningLevel newLevel) {
        if (newLevel.isMoreSevereThan(this.warningLevel)) {
            this.warningLevel = newLevel;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 标记响应 SLA 达标
     */
    public void markResponseCompliant() {
        this.responseStatus = SLAStatus.COMPLIANT;
        this.actualResponseTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 标记响应 SLA 预警
     */
    public void markResponseWarning() {
        this.responseStatus = SLAStatus.WARNING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 标记响应 SLA 超时
     */
    public void markResponseBreached() {
        this.responseStatus = SLAStatus.BREACHED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 标记解决 SLA 达标
     */
    public void markResolutionCompliant() {
        this.resolutionStatus = SLAStatus.COMPLIANT;
        this.actualResolutionTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 标记解决 SLA 超时
     */
    public void markResolutionBreached() {
        this.resolutionStatus = SLAStatus.BREACHED;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String getId() {
        return id != null ? id.getValue() : null;
    }
}
