package com.hkt.iot.workflow.infrastructure.persistence.jpa;

import com.hkt.iot.workflow.infrastructure.persistence.po.FlowNodePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 流程节点 JPA Repository
 *
 * @author HKT IoT Team
 */
@Repository
public interface FlowNodeJpaRepository extends JpaRepository<FlowNodePO, String> {

    /**
     * 根据流程定义 ID 查找流程节点列表
     */
    List<FlowNodePO> findByWorkflowDefinitionId(String workflowDefinitionId);

    /**
     * 根据流程定义 ID 和节点键查找流程节点
     */
    Optional<FlowNodePO> findByWorkflowDefinitionIdAndNodeKey(
            String workflowDefinitionId,
            String nodeKey);

    /**
     * 根据节点键查找流程节点
     */
    Optional<FlowNodePO> findByNodeKey(String nodeKey);

    /**
     * 根据租户 ID 查找流程节点列表
     */
    List<FlowNodePO> findByTenantId(String tenantId);

    /**
     * 根据流程定义 ID 删除所有节点
     */
    void deleteByWorkflowDefinitionId(String workflowDefinitionId);
}
