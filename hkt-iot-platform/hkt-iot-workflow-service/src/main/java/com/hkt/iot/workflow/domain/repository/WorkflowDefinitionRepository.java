package com.hkt.iot.workflow.domain.repository;

import com.hkt.iot.workflow.domain.model.aggregate.WorkflowDefinition;
import com.hkt.iot.workflow.domain.model.valueobject.*;

import java.util.List;
import java.util.Optional;

/**
 * 流程定义仓储接口
 *
 * @author HKT IoT Team
 */
public interface WorkflowDefinitionRepository {

    /**
     * 保存流程定义
     */
    WorkflowDefinition save(WorkflowDefinition workflowDefinition);

    /**
     * 根据 ID 查找流程定义
     */
    Optional<WorkflowDefinition> findById(WorkflowDefinitionId id);

    /**
     * 根据流程定义键查找流程定义
     */
    Optional<WorkflowDefinition> findByKey(WorkflowDefinitionKey key);

    /**
     * 根据租户 ID 查找流程定义列表
     */
    List<WorkflowDefinition> findByTenantId(TenantId tenantId);

    /**
     * 根据状态查找流程定义列表
     */
    List<WorkflowDefinition> findByStatus(WorkflowDefinitionStatus status);

    /**
     * 根据租户 ID 和状态查找流程定义列表
     */
    List<WorkflowDefinition> findByTenantIdAndStatus(TenantId tenantId, WorkflowDefinitionStatus status);

    /**
     * 根据流程定义键和版本查找流程定义
     */
    Optional<WorkflowDefinition> findByKeyAndVersion(WorkflowDefinitionKey key, String version);

    /**
     * 删除流程定义
     */
    void delete(WorkflowDefinition workflowDefinition);

    /**
     * 检查流程定义键是否已存在
     */
    boolean existsByKey(WorkflowDefinitionKey key);
}
