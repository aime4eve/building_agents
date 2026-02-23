package com.hkt.iot.workflow.application.service;

import com.hkt.iot.workflow.application.command.AssignTaskCommand;
import com.hkt.iot.workflow.application.command.CompleteTaskCommand;
import com.hkt.iot.workflow.application.dto.TaskDTO;
import com.hkt.iot.workflow.application.query.TaskQuery;
import com.hkt.iot.workflow.domain.model.entity.WorkflowTask;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.WorkflowTaskRepository;
import com.hkt.iot.workflow.infrastructure.persistence.po.WorkflowTaskPO;
import com.hkt.iot.workflow.infrastructure.persistence.jpa.WorkflowTaskJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务应用服务
 *
 * @author HKT IoT Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskApplicationService {

    private final WorkflowTaskRepository workflowTaskRepository;
    private final WorkflowTaskJpaRepository workflowTaskJpaRepository;

    /**
     * 查询待办任务列表
     */
    public List<TaskDTO> getPendingTasks(String userId) {
        List<WorkflowTaskPO> tasks = workflowTaskJpaRepository.findByAssignee(userId);
        return tasks.stream()
                .filter(task -> "PENDING".equals(task.getStatus()) || "IN_PROGRESS".equals(task.getStatus()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 查询任务列表
     */
    public List<TaskDTO> listTasks(TaskQuery query) {
        List<WorkflowTaskPO> tasks;
        if (query.getAssignee() != null) {
            tasks = workflowTaskJpaRepository.findByAssignee(query.getAssignee());
        } else if (query.getTenantId() != null && query.getStatus() != null) {
            tasks = workflowTaskJpaRepository.findByTenantIdAndState(query.getTenantId(), query.getStatus());
        } else if (query.getProcessInstanceId() != null) {
            tasks = workflowTaskJpaRepository.findByProcessInstanceId(query.getProcessInstanceId());
        } else {
            tasks = workflowTaskJpaRepository.findAll();
        }
        return tasks.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * 分配任务
     */
    @Transactional
    public void assignTask(AssignTaskCommand command) {
        WorkflowTaskPO taskPO = workflowTaskJpaRepository.findById(command.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("任务不存在：" + command.getTaskId()));

        taskPO.setAssignee(command.getAssigneeId());
        taskPO.setStatus("IN_PROGRESS");
        taskPO.setUpdatedAt(java.time.LocalDateTime.now());

        workflowTaskJpaRepository.save(taskPO);
        log.info("Assigned task: taskId={}, assignee={}", command.getTaskId(), command.getAssigneeId());
    }

    /**
     * 完成任务
     */
    @Transactional
    public void completeTask(CompleteTaskCommand command) {
        WorkflowTaskPO taskPO = workflowTaskJpaRepository.findById(command.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("任务不存在：" + command.getTaskId()));

        taskPO.setStatus("COMPLETED");
        taskPO.setCompletedAt(java.time.LocalDateTime.now());
        taskPO.setUpdatedAt(java.time.LocalDateTime.now());

        workflowTaskJpaRepository.save(taskPO);
        log.info("Completed task: taskId={}", command.getTaskId());
    }

    /**
     * 转换为 DTO
     */
    private TaskDTO toDTO(WorkflowTaskPO po) {
        return TaskDTO.builder()
                .taskId(po.getId())
                .taskName(po.getTaskName())
                .taskType(po.getTaskType())
                .status(po.getStatus())
                .processInstanceId(po.getProcessInstanceId())
                .assignee(po.getAssignee())
                .tenantId(po.getTenantId())
                .createdAt(po.getCreatedAt())
                .dueDate(po.getDueDate())
                .build();
    }
}
