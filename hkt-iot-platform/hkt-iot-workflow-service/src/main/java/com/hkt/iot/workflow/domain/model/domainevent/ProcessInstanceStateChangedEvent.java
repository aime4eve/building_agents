package com.hkt.iot.workflow.domain.model.domainevent;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 流程实例状态变更领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class ProcessInstanceStateChangedEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final ProcessInstanceId processInstanceId;
    private final ProcessDefinitionKey processDefinitionKey;
    private final BusinessKey businessKey;
    private final TenantId tenantId;
    private final ProcessInstanceState previousState;
    private final ProcessInstanceState currentState;

    public ProcessInstanceStateChangedEvent(
            ProcessInstanceId processInstanceId,
            ProcessDefinitionKey processDefinitionKey,
            BusinessKey businessKey,
            TenantId tenantId,
            ProcessInstanceState previousState,
            ProcessInstanceState currentState,
            LocalDateTime occurredAt) {
        this.eventId = UUID.randomUUID().toString();
        this.processInstanceId = Objects.requireNonNull(processInstanceId);
        this.processDefinitionKey = Objects.requireNonNull(processDefinitionKey);
        this.businessKey = Objects.requireNonNull(businessKey);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.previousState = Objects.requireNonNull(previousState);
        this.currentState = Objects.requireNonNull(currentState);
        this.occurredAt = Objects.requireNonNull(occurredAt);
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
        return "ProcessInstanceStateChanged";
    }
}
