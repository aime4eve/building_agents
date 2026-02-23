package com.hkt.iot.workflow.domain.repository;

import com.hkt.iot.workflow.domain.model.entity.FlowNode;
import com.hkt.iot.workflow.domain.model.valueobject.FlowNodeKey;
import com.hkt.iot.workflow.domain.model.valueobject.WorkflowDefinitionId;
import com.hkt.iot.workflow.domain.model.valueobject.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * 流程节点仓储接口
 *
 * @author HKT IoT Team
 */
public interface FlowNodeRepository {

    /**
     * 保存流程节点
     */
    FlowNode save(FlowNode flowNode);

    /**
     * 根据 ID 查找流程节点
     */
    Optional<FlowNode> findById(String id);

    /**
     * 根据流程定义 ID 查找流程节点列表
     */
    List<FlowNode> findByWorkflowDefinitionId(WorkflowDefinitionId workflowDefinitionId);

    /**
     * 根据流程定义 ID 和节点键查找流程节点
     */
    Optional<FlowNode> findByWorkflowDefinitionIdAndNodeKey(
            WorkflowDefinitionId workflowDefinitionId,
            FlowNodeKey nodeKey);

    /**
     * 根据节点键查找流程节点
     */
    Optional<FlowNode> findByNodeKey(FlowNodeKey nodeKey);

    /**
     * 根据租户 ID 查找流程节点列表
     */
    List<FlowNode> findByTenantId(TenantId tenantId);

    /**
     * 删除流程节点
     */
    void delete(FlowNode flowNode);

    /**
     * 根据流程定义 ID 删除所有节点
     */
    void deleteByWorkflowDefinitionId(WorkflowDefinitionId workflowDefinitionId);
}
