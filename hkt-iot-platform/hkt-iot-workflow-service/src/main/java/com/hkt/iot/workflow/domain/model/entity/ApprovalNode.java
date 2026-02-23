package com.hkt.iot.workflow.domain.model.entity;

import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 审批节点实体
 *
 * @author HKT IoT Team
 */
@Getter
@NoArgsConstructor
public class ApprovalNode extends FlowNode {

    private ApproverType approverType;
    private String approverIds;
    private Integer timeoutMinutes;
    private MultiApprovalType multiApprovalType;

    private ApprovalNode(
            FlowNodeId id,
            FlowNodeKey nodeKey,
            String nodeName,
            WorkflowDefinitionId workflowDefinitionId,
            Integer order,
            String config,
            TenantId tenantId,
            ApproverType approverType,
            String approverIds,
            Integer timeoutMinutes,
            MultiApprovalType multiApprovalType) {
        super(id, nodeKey, nodeName, FlowNodeType.APPROVAL, workflowDefinitionId, order, config, tenantId);
        this.approverType = approverType;
        this.approverIds = approverIds;
        this.timeoutMinutes = timeoutMinutes;
        this.multiApprovalType = multiApprovalType;
    }

    public static ApprovalNode create(
            FlowNodeKey nodeKey,
            String nodeName,
            WorkflowDefinitionId workflowDefinitionId,
            Integer order,
            String config,
            TenantId tenantId,
            ApproverType approverType,
            String approverIds,
            Integer timeoutMinutes,
            MultiApprovalType multiApprovalType) {
        FlowNodeId id = FlowNodeId.generate();
        return new ApprovalNode(id, nodeKey, nodeName, workflowDefinitionId, order, config, tenantId,
                approverType, approverIds, timeoutMinutes, multiApprovalType);
    }

    public void updateApprovers(ApproverType approverType, String approverIds) {
        this.approverType = approverType;
        this.approverIds = approverIds;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public void updateTimeout(Integer timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public void updateMultiApprovalType(MultiApprovalType multiApprovalType) {
        this.multiApprovalType = multiApprovalType;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    /**
     * 审批人类型枚举
     */
    public enum ApproverType {
        USER,
        ROLE,
        GROUP
    }

    /**
     * 多人审批类型枚举
     */
    public enum MultiApprovalType {
        ANY,
        ALL
    }
}
