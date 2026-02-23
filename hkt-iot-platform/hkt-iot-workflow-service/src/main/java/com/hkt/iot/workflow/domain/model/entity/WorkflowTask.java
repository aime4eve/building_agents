package com.hkt.iot.workflow.domain.model.entity;

import com.hkt.iot.domain.model.Entity;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流任务实体
 *
 * @author HKT IoT Team
 */
@Getter
@NoArgsConstructor
public class WorkflowTask extends Entity<String> {

    private TaskId id;
    private ProcessInstanceId processInstanceId;
    private TaskDefinitionKey taskDefinitionKey;
    private String taskName;
    private TaskType taskType;
    private TaskStatus status;
    private UserId assignee;
    private String candidateGroups;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private TenantId tenantId;
    private LocalDateTime updatedAt;
    private Long version;

    /**
     * 工厂方法：创建任务
     */
    public static WorkflowTask create(
            ProcessInstanceId processInstanceId,
            TaskDefinitionKey taskDefinitionKey,
            String taskName,
            TaskType taskType,
            TenantId tenantId) {
        WorkflowTask task = new WorkflowTask();
        task.id = TaskId.generate();
        task.processInstanceId = processInstanceId;
        task.taskDefinitionKey = taskDefinitionKey;
        task.taskName = taskName;
        task.taskType = taskType;
        task.status = TaskStatus.PENDING;
        task.tenantId = tenantId;
        task.createdAt = LocalDateTime.now();
        task.updatedAt = LocalDateTime.now();
        task.version = 0L;
        return task;
    }

    /**
     * 分配任务
     */
    public void assignTo(UserId assignee) {
        this.assignee = assignee;
        this.status = TaskStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 完成任务
     */
    public void complete() {
        this.status = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 取消任务
     */
    public void cancel() {
        this.status = TaskStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 失败任务
     */
    public void fail() {
        this.status = TaskStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String getId() {
        return id != null ? id.getValue() : null;
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
