package com.hkt.iot.workflow.infrastructure.persistence.jpa;

import com.hkt.iot.workflow.infrastructure.persistence.po.WorkflowDefinitionPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 流程定义 JPA Repository
 *
 * @author HKT IoT Team
 */
@Repository
public interface WorkflowDefinitionJpaRepository extends JpaRepository<WorkflowDefinitionPO, String> {

    /**
     * 根据流程定义键查找
     */
    Optional<WorkflowDefinitionPO> findByDefinitionKey(String definitionKey);

    /**
     * 根据租户 ID 查找
     */
    List<WorkflowDefinitionPO> findByTenantId(String tenantId);

    /**
     * 根据状态查找
     */
    List<WorkflowDefinitionPO> findByStatus(String status);

    /**
     * 根据租户 ID 和状态查找
     */
    List<WorkflowDefinitionPO> findByTenantIdAndStatus(String tenantId, String status);

    /**
     * 根据流程定义键和版本查找
     */
    Optional<WorkflowDefinitionPO> findByDefinitionKeyAndVersion(String definitionKey, String version);

    /**
     * 检查流程定义键是否存在
     */
    boolean existsByDefinitionKey(String definitionKey);

    /**
     * 带乐观锁的查找
     */
    @Lock(jakarta.persistence.LockModeType.OPTIMISTIC)
    @Query("SELECT w FROM WorkflowDefinitionPO w WHERE w.id = :id")
    Optional<WorkflowDefinitionPO> findByIdWithLock(String id);
}
