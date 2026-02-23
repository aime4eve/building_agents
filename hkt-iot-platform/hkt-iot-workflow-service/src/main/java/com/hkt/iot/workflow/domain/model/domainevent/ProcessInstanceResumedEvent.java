package com.hkt.iot.workflow.domain.model.domainevent;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 流程实例恢复领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class ProcessInstanceResumedEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final ProcessInstanceId processInstanceId;
    private final TenantId tenantId;

    public ProcessInstanceResumedEvent(
            ProcessInstanceId processInstanceId,
            TenantId tenantId,
            LocalDateTime occurredAt) {
        this.eventId = UUID.randomUUID().toString();
        this.processInstanceId = Objects.requireNonNull(processInstanceId);
        this.tenantId = Objects.requireNonNull(tenantId);
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
        return "ProcessInstanceResumed";
    }
}
