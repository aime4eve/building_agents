package com.hkt.iot.workflow.infrastructure.persistence.mapper;

import com.hkt.iot.workflow.domain.model.entity.WorkflowTask;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.infrastructure.persistence.po.WorkflowTaskPO;
import org.springframework.stereotype.Component;

/**
 * 工作流任务领域对象与 PO 对象映射器
 *
 * @author HKT IoT Team
 */
@Component
public class WorkflowTaskMapper {

    public WorkflowTaskPO toPO(WorkflowTask domain) {
        return WorkflowTaskPO.builder()
                .id(domain.getId().getValue())
                .processInstanceId(domain.getProcessInstanceId().getValue())
                .taskDefinitionKey(domain.getTaskDefinitionKey().getValue())
                .taskName(domain.getTaskName())
                .taskType(domain.getTaskType().name())
                .status(domain.getStatus().name())
                .assignee(domain.getAssignee() != null ? domain.getAssignee().getValue() : null)
                .candidateGroups(domain.getCandidateGroups())
                .createdAt(domain.getCreatedAt())
                .dueDate(domain.getDueDate())
                .completedAt(domain.getCompletedAt())
                .tenantId(domain.getTenantId().getValue())
                .updatedAt(domain.getUpdatedAt())
                .version(domain.getVersion())
                .deleted(false)
                .build();
    }

    public WorkflowTask toDomain(WorkflowTaskPO po) {
        WorkflowTask task = new WorkflowTask();
        task.id = TaskId.of(po.getId());
        task.processInstanceId = ProcessInstanceId.of(po.getProcessInstanceId());
        task.taskDefinitionKey = TaskDefinitionKey.of(po.getTaskDefinitionKey());
        task.taskName = po.getTaskName();
        task.taskType = TaskType.valueOf(po.getTaskType());
        task.status = TaskStatus.valueOf(po.getStatus());
        task.assignee = po.getAssignee() != null ? UserId.of(po.getAssignee()) : null;
        task.candidateGroups = po.getCandidateGroups();
        task.createdAt = po.getCreatedAt();
        task.dueDate = po.getDueDate();
        task.completedAt = po.getCompletedAt();
        task.tenantId = TenantId.of(po.getTenantId());
        task.updatedAt = po.getUpdatedAt();
        task.version = po.getVersion();
        return task;
    }
}

/**
 * 任务定义键 - 值对象
 */
@lombok.Value
@lombok.EqualsAndHashCode(of = "value")
class TaskDefinitionKey {
    String value;

    private TaskDefinitionKey(String value) {
        this.value = java.util.Objects.requireNonNull(value, "TaskDefinitionKey cannot be null");
    }

    public static TaskDefinitionKey of(String value) {
        return new TaskDefinitionKey(value);
    }
}
