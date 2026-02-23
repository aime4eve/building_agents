package com.hkt.iot.workflow.domain.model.domainevent;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 工单取消领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class WorkOrderCancelledEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final WorkOrderId workOrderId;
    private final WorkOrderNo workOrderNo;
    private final UserId cancelledBy;
    private final String cancelReason;
    private final TenantId tenantId;

    public WorkOrderCancelledEvent(
            WorkOrderId workOrderId,
            WorkOrderNo workOrderNo,
            UserId cancelledBy,
            String cancelReason,
            TenantId tenantId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.workOrderId = Objects.requireNonNull(workOrderId);
        this.workOrderNo = Objects.requireNonNull(workOrderNo);
        this.cancelledBy = cancelledBy;
        this.cancelReason = cancelReason;
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
        return "WorkOrderCancelled";
    }
}
