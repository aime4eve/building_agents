package com.hkt.iot.workflow.domain.model.domainevent;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 流程节点执行领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class FlowNodeExecutedEvent implements DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final FlowNodeId flowNodeId;
    private final FlowNodeKey nodeKey;
    private final FlowNodeType nodeType;
    private final WorkflowDefinitionId workflowDefinitionId;
    private final TenantId tenantId;
    private final Map<String, Object> inputVariables;
    private final Map<String, Object> outputVariables;
    private final String executionStatus;

    public FlowNodeExecutedEvent(
            FlowNodeId flowNodeId,
            FlowNodeKey nodeKey,
            FlowNodeType nodeType,
            WorkflowDefinitionId workflowDefinitionId,
            TenantId tenantId,
            Map<String, Object> inputVariables,
            Map<String, Object> outputVariables,
            String executionStatus,
            LocalDateTime occurredAt) {
        this.eventId = UUID.randomUUID().toString();
        this.flowNodeId = Objects.requireNonNull(flowNodeId);
        this.nodeKey = Objects.requireNonNull(nodeKey);
        this.nodeType = Objects.requireNonNull(nodeType);
        this.workflowDefinitionId = Objects.requireNonNull(workflowDefinitionId);
        this.tenantId = Objects.requireNonNull(tenantId);
        this.inputVariables = inputVariables != null ? Map.copyOf(inputVariables) : Map.of();
        this.outputVariables = outputVariables != null ? Map.copyOf(outputVariables) : Map.of();
        this.executionStatus = Objects.requireNonNull(executionStatus);
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
        return "FlowNodeExecuted";
    }
}
