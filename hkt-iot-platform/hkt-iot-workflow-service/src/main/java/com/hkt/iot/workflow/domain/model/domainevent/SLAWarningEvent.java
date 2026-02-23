package com.hkt.iot.workflow.domain.model.domainevent;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * SLA 预警领域事件
 * 当 SLA 即将到达阈值时触发
 *
 * @author HKT IoT Team
 */
@Getter
public class SLAWarningEvent implements DomainEvent {

    private final String eventId;
    private final SLAMonitorId slaMonitorId;
    private final ProcessInstanceId processInstanceId;
    private final TaskId taskId;
    private final SLAWarningLevel warningLevel;
    private final Duration remainingTime;
    private final LocalDateTime occurredAt;

    public SLAWarningEvent(
            SLAMonitorId slaMonitorId,
            ProcessInstanceId processInstanceId,
            TaskId taskId,
            SLAWarningLevel warningLevel,
            Duration remainingTime,
            LocalDateTime occurredAt) {
        this.eventId = UUID.randomUUID().toString();
        this.slaMonitorId = Objects.requireNonNull(slaMonitorId, "slaMonitorId cannot be null");
        this.processInstanceId = Objects.requireNonNull(processInstanceId, "processInstanceId cannot be null");
        this.taskId = taskId;
        this.warningLevel = Objects.requireNonNull(warningLevel, "warningLevel cannot be null");
        this.remainingTime = Objects.requireNonNull(remainingTime, "remainingTime cannot be null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt cannot be null");
    }

    @Override
    public String getAggregateId() {
        return processInstanceId.getValue();
    }

    @Override
    public String getAggregateType() {
        return "SLAMonitor";
    }

    @Override
    public String getEventType() {
        return "SLAWarning";
    }

    public boolean isCritical() {
        return warningLevel == SLAWarningLevel.CRITICAL;
    }

    public long getRemainingMinutes() {
        return remainingTime.toMinutes();
    }
}
