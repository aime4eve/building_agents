package com.hkt.iot.workflow.domain.model.domainevent;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 任务完成领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class TaskCompletedEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final ProcessInstanceId processInstanceId;
    private final TaskId taskId;
    private final TenantId tenantId;
    private final Map<String, Object> variables;

    public TaskCompletedEvent(
            ProcessInstanceId processInstanceId,
            TaskId taskId,
            TenantId tenantId,
            Map<String, Object> variables,
            LocalDateTime occurredAt) {
        this.eventId = UUID.randomUUID().toString();
        this.processInstanceId = Objects.requireNonNull(processInstanceId);
        this.taskId = Objects.requireNonNull(taskId);
        this.tenantId = Objects.requireNonNull(tenantId);
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
        return "TaskCompleted";
    }
}
