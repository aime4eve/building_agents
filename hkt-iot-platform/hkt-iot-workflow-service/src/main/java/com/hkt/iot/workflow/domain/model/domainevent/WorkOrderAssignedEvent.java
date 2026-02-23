package com.hkt.iot.workflow.domain.model.domainevent;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 工单分配领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class WorkOrderAssignedEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final WorkOrderId workOrderId;
    private final WorkOrderNo workOrderNo;
    private final UserId assigneeId;
    private final UserId assignedBy;
    private final TenantId tenantId;

    public WorkOrderAssignedEvent(
            WorkOrderId workOrderId,
            WorkOrderNo workOrderNo,
            UserId assigneeId,
            UserId assignedBy,
            TenantId tenantId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.workOrderId = Objects.requireNonNull(workOrderId);
        this.workOrderNo = Objects.requireNonNull(workOrderNo);
        this.assigneeId = Objects.requireNonNull(assigneeId);
        this.assignedBy = assignedBy;
        this.tenantId = Objects.requireNonNull(tenantId);
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
        return "WorkOrderAssigned";
    }
}
