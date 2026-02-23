package com.hkt.iot.workflow.infrastructure.persistence.jpa;

import com.hkt.iot.workflow.infrastructure.persistence.po.WorkflowTaskPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 工作流任务 JPA Repository
 *
 * @author HKT IoT Team
 */
@Repository
public interface WorkflowTaskJpaRepository extends JpaRepository<WorkflowTaskPO, String> {

    /**
     * 根据流程实例 ID 查找任务列表
     */
    List<WorkflowTaskPO> findByProcessInstanceId(String processInstanceId);

    /**
     * 根据处理人查找待办任务
     */
    List<WorkflowTaskPO> findByAssignee(String assignee);

    /**
     * 根据租户 ID 和状态查找任务列表
     */
    List<WorkflowTaskPO> findByTenantIdAndState(String tenantId, String status);

    /**
     * 根据流程实例 ID 和状态查找任务列表
     */
    List<WorkflowTaskPO> findByProcessInstanceIdAndState(String processInstanceId, String status);
}
