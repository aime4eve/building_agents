package com.hkt.iot.workflow.domain.model.domainevent;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 流程节点创建领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class FlowNodeCreatedEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final FlowNodeId flowNodeId;
    private final FlowNodeKey nodeKey;
    private final String nodeName;
    private final FlowNodeType nodeType;
    private final WorkflowDefinitionId workflowDefinitionId;
    private final TenantId tenantId;

    public FlowNodeCreatedEvent(
            FlowNodeId flowNodeId,
            FlowNodeKey nodeKey,
            String nodeName,
            FlowNodeType nodeType,
            WorkflowDefinitionId workflowDefinitionId,
            TenantId tenantId,
            LocalDateTime occurredAt) {
        this.eventId = UUID.randomUUID().toString();
        this.flowNodeId = Objects.requireNonNull(flowNodeId);
        this.nodeKey = Objects.requireNonNull(nodeKey);
        this.nodeName = Objects.requireNonNull(nodeName);
        this.nodeType = Objects.requireNonNull(nodeType);
        this.workflowDefinitionId = Objects.requireNonNull(workflowDefinitionId);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }

    @Override
    public String getAggregateId() {
        return flowNodeId.getValue();
    }

    @Override
    public String getAggregateType() {
        return "FlowNode";
    }

    @Override
    public String getEventType() {
        return "FlowNodeCreated";
    }
}
