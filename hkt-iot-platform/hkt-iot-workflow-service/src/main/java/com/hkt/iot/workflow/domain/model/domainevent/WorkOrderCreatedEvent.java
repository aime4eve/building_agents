package com.hkt.iot.workflow.domain.model.domainevent;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 工单创建领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class WorkOrderCreatedEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final WorkOrderId workOrderId;
    private final WorkOrderNo workOrderNo;
    private final WorkOrderType type;
    private final WorkOrderPriority priority;
    private final TenantId tenantId;
    private final UserId reporterId;
    private final String title;

    public WorkOrderCreatedEvent(
            WorkOrderId workOrderId,
            WorkOrderNo workOrderNo,
            WorkOrderType type,
            WorkOrderPriority priority,
            TenantId tenantId,
            UserId reporterId,
            String title) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.workOrderId = Objects.requireNonNull(workOrderId);
        this.workOrderNo = Objects.requireNonNull(workOrderNo);
        this.type = Objects.requireNonNull(type);
        this.priority = Objects.requireNonNull(priority);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.reporterId = reporterId;
        this.title = title;
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
        return "WorkOrderCreated";
    }
}
