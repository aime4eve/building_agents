package com.hkt.iot.workflow.service;

import com.hkt.iot.workflow.domain.model.entity.ProcessHistory;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.ProcessHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

/**
 * 流程历史服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessHistoryService {

    private final ProcessHistoryRepository processHistoryRepository;

    @Transactional
    public ProcessHistory recordProcessStart(
            ProcessInstanceId processInstanceId,
            String activityId,
            String activityName,
            UserId operatorId,
            String operatorName,
            String variables,
            TenantId tenantId) {
        ProcessHistory history = ProcessHistory.createForProcessStart(
                processInstanceId, activityId, activityName,
                operatorId, operatorName, variables, tenantId);
        ProcessHistory saved = processHistoryRepository.save(history);
        log.info("Recorded process start: processInstanceId={}", processInstanceId.getValue());
        return saved;
    }

    @Transactional
    public ProcessHistory recordProcessEnd(
            ProcessInstanceId processInstanceId,
            String activityId,
            String activityName,
            String fromState,
            String toState,
            UserId operatorId,
            String operatorName,
            Duration duration,
            TenantId tenantId) {
        ProcessHistory history = ProcessHistory.createForProcessEnd(
                processInstanceId, activityId, activityName,
                fromState, toState, operatorId, operatorName, duration, tenantId);
        ProcessHistory saved = processHistoryRepository.save(history);
        log.info("Recorded process end: processInstanceId={}", processInstanceId.getValue());
        return saved;
    }

    @Transactional
    public ProcessHistory recordTaskCreate(
            ProcessInstanceId processInstanceId,
            TaskId taskId,
            String activityId,
            String activityName,
            TenantId tenantId) {
        ProcessHistory history = ProcessHistory.createForTaskCreate(
                processInstanceId, taskId, activityId, activityName, tenantId);
        ProcessHistory saved = processHistoryRepository.save(history);
        log.info("Recorded task create: taskId={}", taskId.getValue());
        return saved;
    }

    @Transactional
    public ProcessHistory recordTaskComplete(
            ProcessInstanceId processInstanceId,
            TaskId taskId,
            String activityId,
            String activityName,
            UserId operatorId,
            String operatorName,
            Duration duration,
            TenantId tenantId) {
        ProcessHistory history = ProcessHistory.createForTaskComplete(
                processInstanceId, taskId, activityId, activityName,
                operatorId, operatorName, duration, tenantId);
        ProcessHistory saved = processHistoryRepository.save(history);
        log.info("Recorded task complete: taskId={}", taskId.getValue());
        return saved;
    }

    @Transactional
    public ProcessHistory recordStateChange(
            ProcessInstanceId processInstanceId,
            String activityId,
            String activityName,
            String fromState,
            String toState,
            UserId operatorId,
            String operatorName,
            TenantId tenantId) {
        ProcessHistory history = ProcessHistory.createForStateChange(
                processInstanceId, activityId, activityName,
                fromState, toState, operatorId, operatorName, tenantId);
        ProcessHistory saved = processHistoryRepository.save(history);
        log.info("Recorded state change: processInstanceId={}, {} -> {}", 
                processInstanceId.getValue(), fromState, toState);
        return saved;
    }

    public List<ProcessHistory> getProcessHistory(ProcessInstanceId processInstanceId) {
        return processHistoryRepository.findByProcessInstanceId(processInstanceId);
    }

    public List<ProcessHistory> getProcessHistoryByType(
            ProcessInstanceId processInstanceId, 
            ProcessHistoryType type) {
        return processHistoryRepository.findByProcessInstanceIdAndType(processInstanceId, type);
    }

    public List<ProcessHistory> getTaskHistory(TaskId taskId) {
        return processHistoryRepository.findByTaskId(taskId);
    }
}
