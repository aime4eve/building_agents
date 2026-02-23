package com.hkt.iot.workflow.domain.model.domainevent;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * SLA 监控预警领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class SLAMonitoringWarningEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final SLAMonitorId monitorId;
    private final ProcessInstanceId processInstanceId;
    private final TaskId taskId;
    private final Duration remainingTime;
    private final TenantId tenantId;

    public SLAMonitoringWarningEvent(
            SLAMonitorId monitorId,
            ProcessInstanceId processInstanceId,
            TaskId taskId,
            Duration remainingTime,
            TenantId tenantId,
            LocalDateTime occurredAt) {
        this.eventId = UUID.randomUUID().toString();
        this.monitorId = Objects.requireNonNull(monitorId);
        this.processInstanceId = Objects.requireNonNull(processInstanceId);
        this.taskId = Objects.requireNonNull(taskId);
        this.remainingTime = Objects.requireNonNull(remainingTime);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.occurredAt = Objects.requireNonNull(occurredAt);
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
        return "SLAMonitoringWarning";
    }
}
