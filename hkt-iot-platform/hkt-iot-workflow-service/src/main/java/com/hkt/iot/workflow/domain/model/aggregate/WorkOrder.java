package com.hkt.iot.workflow.domain.model.aggregate;

import com.hkt.iot.domain.model.AggregateRoot;
import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.domainevent.*;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 工单聚合根
 *
 * @author HKT IoT Team
 */
@Getter
@NoArgsConstructor
public class WorkOrder extends AggregateRoot<String> {

    private WorkOrderId id;
    private WorkOrderNo workOrderNo;
    private String title;
    private String description;
    private WorkOrderType type;
    private WorkOrderStatus status;
    private WorkOrderPriority priority;
    private String processInstanceId;
    private String templateId;
    private String spaceId;
    private UserId reporterId;
    private UserId assigneeId;
    private UserId handlerId;
    private LocalDateTime dueTime;
    private LocalDateTime completedAt;
    private TenantId tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public static WorkOrder create(
            String title,
            String description,
            WorkOrderType type,
            WorkOrderPriority priority,
            String templateId,
            String spaceId,
            UserId reporterId,
            TenantId tenantId) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.id = WorkOrderId.generate();
        workOrder.workOrderNo = WorkOrderNo.generate(type);
        workOrder.title = Objects.requireNonNull(title, "title cannot be null");
        workOrder.description = description;
        workOrder.type = Objects.requireNonNull(type, "type cannot be null");
        workOrder.priority = Objects.requireNonNull(priority, "priority cannot be null");
        workOrder.status = WorkOrderStatus.CREATED;
        workOrder.templateId = templateId;
        workOrder.spaceId = spaceId;
        workOrder.reporterId = reporterId;
        workOrder.tenantId = Objects.requireNonNull(tenantId, "tenantId cannot be null");
        workOrder.dueTime = calculateDueTime(priority);
        workOrder.createdAt = LocalDateTime.now();
        workOrder.updatedAt = LocalDateTime.now();
        workOrder.version = 0L;

        workOrder.registerDomainEvent(new WorkOrderCreatedEvent(
                workOrder.id,
                workOrder.workOrderNo,
                workOrder.type,
                workOrder.priority,
                workOrder.tenantId,
                workOrder.reporterId,
                workOrder.title
        ));

        return workOrder;
    }

    public void assign(UserId assigneeId, UserId assignedBy) {
        validateStatusTransition(WorkOrderStatus.ASSIGNED);
        
        this.assigneeId = Objects.requireNonNull(assigneeId, "assigneeId cannot be null");
        this.status = WorkOrderStatus.ASSIGNED;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new WorkOrderAssignedEvent(
                this.id,
                this.workOrderNo,
                this.assigneeId,
                assignedBy,
                this.tenantId
        ));
    }

    public void autoAssign(UserId assigneeId) {
        validateStatusTransition(WorkOrderStatus.PENDING_ASSIGN);
        
        this.status = WorkOrderStatus.PENDING_ASSIGN;
        this.updatedAt = LocalDateTime.now();
    }

    public void startProcess(UserId handlerId) {
        validateStatusTransition(WorkOrderStatus.PROCESSING);
        
        this.handlerId = Objects.requireNonNull(handlerId, "handlerId cannot be null");
        this.status = WorkOrderStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new WorkOrderProcessingEvent(
                this.id,
                this.workOrderNo,
                this.handlerId,
                this.tenantId
        ));
    }

    public void complete() {
        validateStatusTransition(WorkOrderStatus.COMPLETED);
        
        this.status = WorkOrderStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new WorkOrderCompletedEvent(
                this.id,
                this.workOrderNo,
                this.handlerId,
                this.tenantId,
                this.completedAt
        ));
    }

    public void cancel(UserId cancelledBy, String reason) {
        if (!this.status.canTransitionTo(WorkOrderStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel work order in status: " + this.status);
        }
        
        this.status = WorkOrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new WorkOrderCancelledEvent(
                this.id,
                this.workOrderNo,
                cancelledBy,
                reason,
                this.tenantId
        ));
    }

    public void reject(String reason) {
        validateStatusTransition(WorkOrderStatus.REJECTED);
        
        this.status = WorkOrderStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }

    public void submitForConfirmation() {
        validateStatusTransition(WorkOrderStatus.PENDING_CONFIRM);
        
        this.status = WorkOrderStatus.PENDING_CONFIRM;
        this.updatedAt = LocalDateTime.now();
    }

    public void reassign(UserId newAssigneeId, UserId reassignedBy) {
        if (this.status != WorkOrderStatus.ASSIGNED && this.status != WorkOrderStatus.PROCESSING) {
            throw new IllegalStateException("Can only reassign work order in ASSIGNED or PROCESSING status");
        }
        
        this.assigneeId = Objects.requireNonNull(newAssigneeId, "newAssigneeId cannot be null");
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new WorkOrderAssignedEvent(
                this.id,
                this.workOrderNo,
                this.assigneeId,
                reassignedBy,
                this.tenantId
        ));
    }

    public boolean isOverdue() {
        return dueTime != null && LocalDateTime.now().isAfter(dueTime) 
                && status != WorkOrderStatus.COMPLETED 
                && status != WorkOrderStatus.CANCELLED;
    }

    public void bindProcessInstance(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        this.updatedAt = LocalDateTime.now();
    }

    protected void registerDomainEvent(DomainEvent event) {
        this.domainEvents.add(Objects.requireNonNull(event));
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    private void validateStatusTransition(WorkOrderStatus targetStatus) {
        if (!this.status.canTransitionTo(targetStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot transition from %s to %s", this.status, targetStatus));
        }
    }

    private static LocalDateTime calculateDueTime(WorkOrderPriority priority) {
        return LocalDateTime.now().plusHours(priority.getSlaHours());
    }

    @Override
    public String getId() {
        return id != null ? id.getValue() : null;
    }
}
