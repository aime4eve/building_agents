package com.hkt.iot.workflow.domain.model.domainevent;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 流程定义创建领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class WorkflowDefinitionCreatedEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final WorkflowDefinitionId workflowDefinitionId;
    private final WorkflowDefinitionKey workflowDefinitionKey;
    private final String name;
    private final String version;
    private final TenantId tenantId;
    private final UserId createdBy;

    public WorkflowDefinitionCreatedEvent(
            WorkflowDefinitionId workflowDefinitionId,
            WorkflowDefinitionKey workflowDefinitionKey,
            String name,
            String version,
            TenantId tenantId,
            UserId createdBy,
            LocalDateTime occurredAt) {
        this.eventId = UUID.randomUUID().toString();
        this.workflowDefinitionId = Objects.requireNonNull(workflowDefinitionId);
        this.workflowDefinitionKey = Objects.requireNonNull(workflowDefinitionKey);
        this.name = Objects.requireNonNull(name);
        this.version = Objects.requireNonNull(version);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.createdBy = Objects.requireNonNull(createdBy);
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }

    @Override
    public String getAggregateId() {
        return workflowDefinitionId.getValue();
    }

    @Override
    public String getAggregateType() {
        return "WorkflowDefinition";
    }

    @Override
    public String getEventType() {
        return "WorkflowDefinitionCreated";
    }
}
