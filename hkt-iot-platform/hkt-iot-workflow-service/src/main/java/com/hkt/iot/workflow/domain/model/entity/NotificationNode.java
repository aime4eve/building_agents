package com.hkt.iot.workflow.domain.model.entity;

import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 通知节点实体
 *
 * @author HKT IoT Team
 */
@Getter
@NoArgsConstructor
public class NotificationNode extends FlowNode {

    private ChannelType channelType;
    private String templateCode;
    private ReceiverType receiverType;
    private String receiverIds;

    private NotificationNode(
            FlowNodeId id,
            FlowNodeKey nodeKey,
            String nodeName,
            WorkflowDefinitionId workflowDefinitionId,
            Integer order,
            String config,
            TenantId tenantId,
            ChannelType channelType,
            String templateCode,
            ReceiverType receiverType,
            String receiverIds) {
        super(id, nodeKey, nodeName, FlowNodeType.NOTIFICATION, workflowDefinitionId, order, config, tenantId);
        this.channelType = channelType;
        this.templateCode = templateCode;
        this.receiverType = receiverType;
        this.receiverIds = receiverIds;
    }

    public static NotificationNode create(
            FlowNodeKey nodeKey,
            String nodeName,
            WorkflowDefinitionId workflowDefinitionId,
            Integer order,
            String config,
            TenantId tenantId,
            ChannelType channelType,
            String templateCode,
            ReceiverType receiverType,
            String receiverIds) {
        FlowNodeId id = FlowNodeId.generate();
        return new NotificationNode(id, nodeKey, nodeName, workflowDefinitionId, order, config, tenantId,
                channelType, templateCode, receiverType, receiverIds);
    }

    public void updateNotificationConfig(
            ChannelType channelType,
            String templateCode,
            ReceiverType receiverType,
            String receiverIds) {
        this.channelType = channelType;
        this.templateCode = templateCode;
        this.receiverType = receiverType;
        this.receiverIds = receiverIds;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    /**
     * 通知渠道类型枚举
     */
    public enum ChannelType {
        EMAIL,
        SMS,
        WECHAT,
        DINGTALK,
        WEBHOOK
    }

    /**
     * 接收者类型枚举
     */
    public enum ReceiverType {
        USER,
        ROLE,
        GROUP,
        VARIABLE
    }
}
