package com.hkt.iot.workflow.domain.model.domainevent;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 工单完成领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class WorkOrderCompletedEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final WorkOrderId workOrderId;
    private final WorkOrderNo workOrderNo;
    private final UserId handlerId;
    private final TenantId tenantId;
    private final LocalDateTime completedAt;

    public WorkOrderCompletedEvent(
            WorkOrderId workOrderId,
            WorkOrderNo workOrderNo,
            UserId handlerId,
            TenantId tenantId,
            LocalDateTime completedAt) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.workOrderId = Objects.requireNonNull(workOrderId);
        this.workOrderNo = Objects.requireNonNull(workOrderNo);
        this.handlerId = handlerId;
        this.tenantId = Objects.requireNonNull(tenantId);
        this.completedAt = Objects.requireNonNull(completedAt);
    }

    @Override
    public String getAggregateId() {
        return workOrderId.getValue();
    }

    @Override
    public String getAggregateType() {
        return "WorkOrder";
    }

    @Override
    public String getEventType() {
        return "WorkOrderCompleted";
    }
}
