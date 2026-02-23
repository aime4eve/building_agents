package com.hkt.iot.workflow.domain.model.entity;

import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 网关节点实体
 *
 * @author HKT IoT Team
 */
@Getter
@NoArgsConstructor
public class GatewayNode extends FlowNode {

    private GatewayType gatewayType;
    private String conditions;

    private GatewayNode(
            FlowNodeId id,
            FlowNodeKey nodeKey,
            String nodeName,
            WorkflowDefinitionId workflowDefinitionId,
            Integer order,
            String config,
            TenantId tenantId,
            GatewayType gatewayType,
            String conditions) {
        super(id, nodeKey, nodeName, resolveNodeType(gatewayType), workflowDefinitionId, order, config, tenantId);
        this.gatewayType = gatewayType;
        this.conditions = conditions;
    }

    public static GatewayNode create(
            FlowNodeKey nodeKey,
            String nodeName,
            WorkflowDefinitionId workflowDefinitionId,
            Integer order,
            String config,
            TenantId tenantId,
            GatewayType gatewayType,
            String conditions) {
        FlowNodeId id = FlowNodeId.generate();
        return new GatewayNode(id, nodeKey, nodeName, workflowDefinitionId, order, config, tenantId,
                gatewayType, conditions);
    }

    public void updateConditions(String conditions) {
        this.conditions = conditions;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public void updateGatewayType(GatewayType gatewayType) {
        this.gatewayType = gatewayType;
        this.nodeType = resolveNodeType(gatewayType);
        this.updatedAt = java.time.LocalDateTime.now();
    }

    private static FlowNodeType resolveNodeType(GatewayType gatewayType) {
        if (gatewayType == GatewayType.EXCLUSIVE) {
            return FlowNodeType.GATEWAY_EXCLUSIVE;
        } else if (gatewayType == GatewayType.PARALLEL) {
            return FlowNodeType.GATEWAY_PARALLEL;
        }
        return FlowNodeType.GATEWAY_EXCLUSIVE;
    }
}
