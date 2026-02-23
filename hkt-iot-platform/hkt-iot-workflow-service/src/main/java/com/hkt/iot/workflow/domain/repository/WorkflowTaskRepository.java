package com.hkt.iot.workflow.domain.repository;

import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.model.entity.WorkflowTask;

import java.util.List;
import java.util.Optional;

/**
 * 工作流任务仓储接口
 *
 * @author HKT IoT Team
 */
public interface WorkflowTaskRepository {

    /**
     * 保存任务
     */
    WorkflowTask save(WorkflowTask task);

    /**
     * 根据 ID 查找任务
     */
    Optional<WorkflowTask> findById(TaskId id);

    /**
     * 根据流程实例 ID 查找任务列表
     */
    List<WorkflowTask> findByProcessInstanceId(ProcessInstanceId processInstanceId);

    /**
     * 根据处理人查找待办任务
     */
    List<WorkflowTask> findByAssignee(UserId assignee);

    /**
     * 根据租户 ID 和状态查找任务列表
     */
    List<WorkflowTask> findByTenantIdAndState(TenantId tenantId, TaskStatus status);

    /**
     * 删除任务
     */
    void delete(WorkflowTask task);
}
