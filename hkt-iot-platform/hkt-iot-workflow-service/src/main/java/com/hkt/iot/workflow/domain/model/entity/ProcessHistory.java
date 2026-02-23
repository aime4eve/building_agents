package com.hkt.iot.workflow.domain.model.entity;

import com.hkt.iot.domain.model.Entity;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 流程历史实体
 */
@Getter
@NoArgsConstructor
public class ProcessHistory extends Entity<String> {

    private ProcessHistoryId id;
    private ProcessInstanceId processInstanceId;
    private TaskId taskId;
    private ProcessHistoryType type;
    private String activityId;
    private String activityName;
    private String fromState;
    private String toState;
    private UserId operatorId;
    private String operatorName;
    private String variables;
    private Duration duration;
    private TenantId tenantId;
    private LocalDateTime createdAt;

    public static ProcessHistory createForProcessStart(
            ProcessInstanceId processInstanceId,
            String activityId,
            String activityName,
            UserId operatorId,
            String operatorName,
            String variables,
            TenantId tenantId) {
        ProcessHistory history = new ProcessHistory();
        history.id = ProcessHistoryId.generate();
        history.processInstanceId = processInstanceId;
        history.type = ProcessHistoryType.PROCESS_START;
        history.activityId = activityId;
        history.activityName = activityName;
        history.operatorId = operatorId;
        history.operatorName = operatorName;
        history.variables = variables;
        history.tenantId = tenantId;
        history.createdAt = LocalDateTime.now();
        return history;
    }

    public static ProcessHistory createForProcessEnd(
            ProcessInstanceId processInstanceId,
            String activityId,
            String activityName,
            String fromState,
            String toState,
            UserId operatorId,
            String operatorName,
            Duration duration,
            TenantId tenantId) {
        ProcessHistory history = new ProcessHistory();
        history.id = ProcessHistoryId.generate();
        history.processInstanceId = processInstanceId;
        history.type = ProcessHistoryType.PROCESS_END;
        history.activityId = activityId;
        history.activityName = activityName;
        history.fromState = fromState;
        history.toState = toState;
        history.operatorId = operatorId;
        history.operatorName = operatorName;
        history.duration = duration;
        history.tenantId = tenantId;
        history.createdAt = LocalDateTime.now();
        return history;
    }

    public static ProcessHistory createForTaskCreate(
            ProcessInstanceId processInstanceId,
            TaskId taskId,
            String activityId,
            String activityName,
            TenantId tenantId) {
        ProcessHistory history = new ProcessHistory();
        history.id = ProcessHistoryId.generate();
        history.processInstanceId = processInstanceId;
        history.taskId = taskId;
        history.type = ProcessHistoryType.TASK_CREATE;
        history.activityId = activityId;
        history.activityName = activityName;
        history.tenantId = tenantId;
        history.createdAt = LocalDateTime.now();
        return history;
    }

    public static ProcessHistory createForTaskComplete(
            ProcessInstanceId processInstanceId,
            TaskId taskId,
            String activityId,
            String activityName,
            UserId operatorId,
            String operatorName,
            Duration duration,
            TenantId tenantId) {
        ProcessHistory history = new ProcessHistory();
        history.id = ProcessHistoryId.generate();
        history.processInstanceId = processInstanceId;
        history.taskId = taskId;
        history.type = ProcessHistoryType.TASK_COMPLETE;
        history.activityId = activityId;
        history.activityName = activityName;
        history.operatorId = operatorId;
        history.operatorName = operatorName;
        history.duration = duration;
        history.tenantId = tenantId;
        history.createdAt = LocalDateTime.now();
        return history;
    }

    public static ProcessHistory createForStateChange(
            ProcessInstanceId processInstanceId,
            String activityId,
            String activityName,
            String fromState,
            String toState,
            UserId operatorId,
            String operatorName,
            TenantId tenantId) {
        ProcessHistory history = new ProcessHistory();
        history.id = ProcessHistoryId.generate();
        history.processInstanceId = processInstanceId;
        history.type = ProcessHistoryType.STATE_CHANGE;
        history.activityId = activityId;
        history.activityName = activityName;
        history.fromState = fromState;
        history.toState = toState;
        history.operatorId = operatorId;
        history.operatorName = operatorName;
        history.tenantId = tenantId;
        history.createdAt = LocalDateTime.now();
        return history;
    }

    @Override
    public String getId() {
        return id != null ? id.getValue() : null;
    }
}
