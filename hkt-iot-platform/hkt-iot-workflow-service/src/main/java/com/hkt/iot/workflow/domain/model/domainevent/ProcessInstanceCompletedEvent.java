package com.hkt.iot.workflow.domain.model.domainevent;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 流程实例完成领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class ProcessInstanceCompletedEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final ProcessInstanceId processInstanceId;
    private final ProcessDefinitionKey processDefinitionKey;
    private final BusinessKey businessKey;
    private final TenantId tenantId;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;

    public ProcessInstanceCompletedEvent(
            ProcessInstanceId processInstanceId,
            ProcessDefinitionKey processDefinitionKey,
            BusinessKey businessKey,
            TenantId tenantId,
            LocalDateTime startedAt,
            LocalDateTime completedAt) {
        this.eventId = UUID.randomUUID().toString();
        this.processInstanceId = Objects.requireNonNull(processInstanceId);
        this.processDefinitionKey = Objects.requireNonNull(processDefinitionKey);
        this.businessKey = Objects.requireNonNull(businessKey);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.startedAt = Objects.requireNonNull(startedAt);
        this.completedAt = Objects.requireNonNull(completedAt);
        this.occurredAt = completedAt;
    }

    @Override
    public String getAggregateId() {
        return processInstanceId.getValue();
    }

    @Override
    public String getAggregateType() {
        return "ProcessInstance";
    }

    @Override
    public String getEventType() {
        return "ProcessInstanceCompleted";
    }
}
