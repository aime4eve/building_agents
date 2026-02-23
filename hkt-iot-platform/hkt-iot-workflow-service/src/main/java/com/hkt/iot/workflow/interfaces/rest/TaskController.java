package com.hkt.iot.workflow.interfaces.rest;

import com.hkt.iot.workflow.application.command.AssignTaskCommand;
import com.hkt.iot.workflow.application.command.CompleteTaskCommand;
import com.hkt.iot.workflow.application.dto.ApiResponse;
import com.hkt.iot.workflow.application.dto.TaskDTO;
import com.hkt.iot.workflow.application.query.TaskQuery;
import com.hkt.iot.workflow.application.service.TaskApplicationService;
import com.hkt.iot.workflow.interfaces.dto.CompleteTaskRequest;
import com.hkt.iot.workflow.interfaces.dto.CompleteTaskResponse;
import com.hkt.iot.workflow.interfaces.dto.TaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务控制器
 *
 * @author HKT IoT Team
 */
@RestController
@RequestMapping("/api/v1/workflow-engine")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "任务管理 API", description = "工作流任务管理接口")
public class TaskController {

    private final TaskApplicationService taskApplicationService;

    /**
     * 查询用户待办任务
     */
    @GetMapping("/task/pending")
    @Operation(summary = "查询用户待办任务")
    public ApiResponse<List<TaskResponse>> getPendingTasks(@RequestParam String userId) {
        List<TaskDTO> list = taskApplicationService.getPendingTasks(userId);
        List<TaskResponse> response = list.stream()
                .map(dto -> TaskResponse.builder()
                        .taskId(dto.getTaskId())
                        .taskName(dto.getTaskName())
                        .processInstanceId(dto.getProcessInstanceId())
                        .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt().toString() : null)
                        .build())
                .collect(Collectors.toList());
        return ApiResponse.success(response);
    }

    /**
     * 查询任务列表
     */
    @GetMapping("/task/list")
    @Operation(summary = "查询任务列表")
    public ApiResponse<List<TaskDTO>> listTasks(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        TaskQuery query = TaskQuery.builder()
                .tenantId(tenantId)
                .assignee(assignee)
                .status(status)
                .page(page)
                .size(size)
                .build();
        List<TaskDTO> list = taskApplicationService.listTasks(query);
        return ApiResponse.success(list);
    }

    /**
     * 分配任务
     */
    @PostMapping("/task/{taskId}/assign")
    @Operation(summary = "分配任务")
    public ApiResponse<Void> assignTask(
            @PathVariable String taskId,
            @RequestParam String assigneeId) {
        AssignTaskCommand command = new AssignTaskCommand(taskId, assigneeId, null);
        taskApplicationService.assignTask(command);
        return ApiResponse.success("任务已分配", null);
    }

    /**
     * 完成任务
     */
    @PostMapping("/task/{taskId}/complete")
    @Operation(summary = "完成任务")
    public ApiResponse<CompleteTaskResponse> completeTask(
            @PathVariable String taskId,
            @RequestBody CompleteTaskRequest request) {
        CompleteTaskCommand command = new CompleteTaskCommand(
                taskId,
                request.getUserId(),
                request.getComment(),
                request.getVariables()
        );
        taskApplicationService.completeTask(command);
        CompleteTaskResponse response = CompleteTaskResponse.builder()
                .taskId(taskId)
                .processInstanceId(null)
                .build();
        return ApiResponse.success(response);
    }
}
