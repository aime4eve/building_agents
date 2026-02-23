package com.hkt.iot.workflow.domain.model.entity;

import com.hkt.iot.workflow.domain.model.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 流程节点实体测试
 */
@DisplayName("流程节点实体测试")
class FlowNodeTest {

    @Test
    @DisplayName("测试创建流程节点")
    void testCreateFlowNode() {
        FlowNode node = FlowNode.create(
                FlowNodeKey.of("start-node"),
                "开始节点",
                FlowNodeType.START,
                WorkflowDefinitionId.of("wf-001"),
                1,
                "{}",
                TenantId.of("tenant-001")
        );

        assertNotNull(node);
        assertNotNull(node.getId());
        assertEquals("start-node", node.getNodeKey().getValue());
        assertEquals("开始节点", node.getNodeName());
        assertEquals(FlowNodeType.START, node.getNodeType());
        assertEquals(1, node.getOrder());
    }

    @Test
    @DisplayName("测试更新节点配置")
    void testUpdateConfig() {
        FlowNode node = createTestNode();
        String newConfig = "{\"timeout\": 3600}";

        node.updateConfig(newConfig);

        assertEquals(newConfig, node.getConfig());
        assertNotNull(node.getUpdatedAt());
    }

    @Test
    @DisplayName("测试创建审批节点")
    void testCreateApprovalNode() {
        ApprovalNode node = ApprovalNode.create(
                FlowNodeKey.of("approval-1"),
                "经理审批",
                WorkflowDefinitionId.of("wf-001"),
                2,
                "{}",
                TenantId.of("tenant-001"),
                ApprovalNode.ApproverType.USER,
                "user-001,user-002",
                60,
                ApprovalNode.MultiApprovalType.ANY
        );

        assertNotNull(node);
        assertEquals(FlowNodeType.APPROVAL, node.getNodeType());
        assertEquals(ApprovalNode.ApproverType.USER, node.getApproverType());
        assertEquals(60, node.getTimeoutMinutes());
        assertEquals(ApprovalNode.MultiApprovalType.ANY, node.getMultiApprovalType());
    }

    @Test
    @DisplayName("测试创建服务调用节点")
    void testCreateServiceNode() {
        ServiceNode node = ServiceNode.create(
                FlowNodeKey.of("service-1"),
                "发送通知",
                WorkflowDefinitionId.of("wf-001"),
                3,
                "{}",
                TenantId.of("tenant-001"),
                "http://notification-service/api/send",
                "POST",
                "{\"template\": \"approval_notice\"}",
                30
        );

        assertNotNull(node);
        assertEquals(FlowNodeType.SERVICE, node.getNodeType());
        assertEquals("http://notification-service/api/send", node.getServiceUrl());
        assertEquals("POST", node.getHttpMethod());
        assertEquals(30, node.getTimeoutSeconds());
    }

    @Test
    @DisplayName("测试创建网关节点")
    void testCreateGatewayNode() {
        GatewayNode node = GatewayNode.create(
                FlowNodeKey.of("gateway-1"),
                "条件分支",
                WorkflowDefinitionId.of("wf-001"),
                4,
                "{}",
                TenantId.of("tenant-001"),
                GatewayType.EXCLUSIVE,
                "[{\"condition\": \"amount > 10000\", \"target\": \"director-approval\"}]"
        );

        assertNotNull(node);
        assertEquals(FlowNodeType.GATEWAY_EXCLUSIVE, node.getNodeType());
        assertEquals(GatewayType.EXCLUSIVE, node.getGatewayType());
        assertNotNull(node.getConditions());
    }

    @Test
    @DisplayName("测试创建通知节点")
    void testCreateNotificationNode() {
        NotificationNode node = NotificationNode.create(
                FlowNodeKey.of("notify-1"),
                "发送审批通知",
                WorkflowDefinitionId.of("wf-001"),
                5,
                "{}",
                TenantId.of("tenant-001"),
                "EMAIL,SMS",
                "approval_notification",
                "USER",
                "applicant"
        );

        assertNotNull(node);
        assertEquals(FlowNodeType.NOTIFICATION, node.getNodeType());
        assertEquals("EMAIL,SMS", node.getChannelType());
        assertEquals("approval_notification", node.getTemplateCode());
    }

    private FlowNode createTestNode() {
        return FlowNode.create(
                FlowNodeKey.of("test-node"),
                "测试节点",
                FlowNodeType.SERVICE,
                WorkflowDefinitionId.of("wf-001"),
                1,
                "{}",
                TenantId.of("tenant-001")
        );
    }
}
