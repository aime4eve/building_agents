package com.hkt.iot.workflow.domain.model.domainevent;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 流程实例启动领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class ProcessInstanceStartedEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final ProcessInstanceId processInstanceId;
    private final ProcessDefinitionKey processDefinitionKey;
    private final BusinessKey businessKey;
    private final TenantId tenantId;
    private final UserId startedBy;
    private final Map<String, Object> variables;

    public ProcessInstanceStartedEvent(
            ProcessInstanceId processInstanceId,
            ProcessDefinitionKey processDefinitionKey,
            BusinessKey businessKey,
            TenantId tenantId,
            UserId startedBy,
            Map<String, Object> variables,
            LocalDateTime occurredAt) {
        this.eventId = UUID.randomUUID().toString();
        this.processInstanceId = Objects.requireNonNull(processInstanceId);
        this.processDefinitionKey = Objects.requireNonNull(processDefinitionKey);
        this.businessKey = Objects.requireNonNull(businessKey);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.startedBy = Objects.requireNonNull(startedBy);
        this.variables = Map.copyOf(variables);
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
        return "ProcessInstanceStarted";
    }
}
