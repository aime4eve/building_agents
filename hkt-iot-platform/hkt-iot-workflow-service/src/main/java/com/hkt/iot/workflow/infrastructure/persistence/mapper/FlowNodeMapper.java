package com.hkt.iot.workflow.infrastructure.persistence.mapper;

import com.hkt.iot.workflow.domain.model.entity.*;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.infrastructure.persistence.po.FlowNodePO;
import org.springframework.stereotype.Component;

/**
 * 流程节点领域对象与 PO 对象映射器
 *
 * @author HKT IoT Team
 */
@Component
public class FlowNodeMapper {

    public FlowNodePO toPO(FlowNode domain) {
        FlowNodePO.FlowNodePOBuilder builder = FlowNodePO.builder()
                .id(domain.getId())
                .nodeKey(domain.getNodeKey().getValue())
                .nodeName(domain.getNodeName())
                .nodeType(domain.getNodeType().name())
                .workflowDefinitionId(domain.getWorkflowDefinitionId().getValue())
                .orderNum(domain.getOrder())
                .config(domain.getConfig())
                .tenantId(domain.getTenantId().getValue())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .deleted(false);

        return builder.build();
    }

    public FlowNode toDomain(FlowNodePO po) {
        FlowNodeType nodeType = FlowNodeType.valueOf(po.getNodeType());

        return switch (nodeType) {
            case APPROVAL -> mapToApprovalNode(po);
            case SERVICE -> mapToServiceNode(po);
            case GATEWAY_EXCLUSIVE, GATEWAY_PARALLEL -> mapToGatewayNode(po, nodeType);
            case NOTIFICATION -> mapToNotificationNode(po);
            default -> mapToBaseFlowNode(po, nodeType);
        };
    }

    private FlowNode mapToBaseFlowNode(FlowNodePO po, FlowNodeType nodeType) {
        FlowNode node = new FlowNode();
        node.id = FlowNodeId.of(po.getId());
        node.nodeKey = FlowNodeKey.of(po.getNodeKey());
        node.nodeName = po.getNodeName();
        node.nodeType = nodeType;
        node.workflowDefinitionId = WorkflowDefinitionId.of(po.getWorkflowDefinitionId());
        node.order = po.getOrderNum();
        node.config = po.getConfig();
        node.tenantId = TenantId.of(po.getTenantId());
        node.createdAt = po.getCreatedAt();
        node.updatedAt = po.getUpdatedAt();
        return node;
    }

    private ApprovalNode mapToApprovalNode(FlowNodePO po) {
        ApprovalNode node = new ApprovalNode();
        node.id = FlowNodeId.of(po.getId());
        node.nodeKey = FlowNodeKey.of(po.getNodeKey());
        node.nodeName = po.getNodeName();
        node.nodeType = FlowNodeType.APPROVAL;
        node.workflowDefinitionId = WorkflowDefinitionId.of(po.getWorkflowDefinitionId());
        node.order = po.getOrderNum();
        node.config = po.getConfig();
        node.tenantId = TenantId.of(po.getTenantId());
        node.createdAt = po.getCreatedAt();
        node.updatedAt = po.getUpdatedAt();
        return node;
    }

    private ServiceNode mapToServiceNode(FlowNodePO po) {
        ServiceNode node = new ServiceNode();
        node.id = FlowNodeId.of(po.getId());
        node.nodeKey = FlowNodeKey.of(po.getNodeKey());
        node.nodeName = po.getNodeName();
        node.nodeType = FlowNodeType.SERVICE;
        node.workflowDefinitionId = WorkflowDefinitionId.of(po.getWorkflowDefinitionId());
        node.order = po.getOrderNum();
        node.config = po.getConfig();
        node.tenantId = TenantId.of(po.getTenantId());
        node.createdAt = po.getCreatedAt();
        node.updatedAt = po.getUpdatedAt();
        return node;
    }

    private GatewayNode mapToGatewayNode(FlowNodePO po, FlowNodeType nodeType) {
        GatewayNode node = new GatewayNode();
        node.id = FlowNodeId.of(po.getId());
        node.nodeKey = FlowNodeKey.of(po.getNodeKey());
        node.nodeName = po.getNodeName();
        node.nodeType = nodeType;
        node.workflowDefinitionId = WorkflowDefinitionId.of(po.getWorkflowDefinitionId());
        node.order = po.getOrderNum();
        node.config = po.getConfig();
        node.tenantId = TenantId.of(po.getTenantId());
        node.createdAt = po.getCreatedAt();
        node.updatedAt = po.getUpdatedAt();
        return node;
    }

    private NotificationNode mapToNotificationNode(FlowNodePO po) {
        NotificationNode node = new NotificationNode();
        node.id = FlowNodeId.of(po.getId());
        node.nodeKey = FlowNodeKey.of(po.getNodeKey());
        node.nodeName = po.getNodeName();
        node.nodeType = FlowNodeType.NOTIFICATION;
        node.workflowDefinitionId = WorkflowDefinitionId.of(po.getWorkflowDefinitionId());
        node.order = po.getOrderNum();
        node.config = po.getConfig();
        node.tenantId = TenantId.of(po.getTenantId());
        node.createdAt = po.getCreatedAt();
        node.updatedAt = po.getUpdatedAt();
        return node;
    }
}
