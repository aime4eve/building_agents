package com.hkt.iot.workflow.interfaces.rest;

import com.hkt.iot.workflow.application.command.*;
import com.hkt.iot.workflow.application.dto.ApiResponse;
import com.hkt.iot.workflow.application.dto.ProcessInstanceDTO;
import com.hkt.iot.workflow.application.query.ProcessInstanceQuery;
import com.hkt.iot.workflow.application.service.WorkflowApplicationService;
import com.hkt.iot.workflow.interfaces.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工作流引擎控制器
 *
 * @author HKT IoT Team
 */
@RestController
@RequestMapping("/api/v1/workflow-engine")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "工作流引擎 API", description = "工作流引擎核心接口")
public class WorkflowController {

    private final WorkflowApplicationService workflowApplicationService;

    /**
     * 启动流程实例
     */
    @PostMapping("/process/start")
    @Operation(summary = "启动流程实例")
    public ApiResponse<ProcessInstanceResponse> startProcess(@RequestBody StartProcessRequest request) {
        StartProcessCommand command = new StartProcessCommand(
                request.getTenantId(),
                request.getProcessDefinitionKey(),
                request.getBusinessKey(),
                request.getStartedBy(),
                request.getVariables()
        );
        ProcessInstanceDTO dto = workflowApplicationService.startProcess(command);
        ProcessInstanceResponse response = ProcessInstanceResponse.builder()
                .processInstanceId(dto.getProcessInstanceId())
                .businessKey(dto.getBusinessKey())
                .definitionId(dto.getProcessDefinitionKey())
                .startedAt(dto.getStartedAt() != null ? dto.getStartedAt().toString() : null)
                .currentActivityId(dto.getCurrentActivityId())
                .build();
        return ApiResponse.success(response);
    }

    /**
     * 查询流程实例状态
     */
    @GetMapping("/process/{processInstanceId}/status")
    @Operation(summary = "查询流程实例状态")
    public ApiResponse<ProcessInstanceStatusResponse> getProcessStatus(
            @PathVariable String processInstanceId) {
        ProcessInstanceDTO dto = workflowApplicationService.getProcessInstance(processInstanceId);
        ProcessInstanceStatusResponse response = ProcessInstanceStatusResponse.builder()
                .processInstanceId(dto.getProcessInstanceId())
                .businessKey(dto.getBusinessKey())
                .processDefinitionKey(dto.getProcessDefinitionKey())
                .state(dto.getState())
                .startedAt(dto.getStartedAt() != null ? dto.getStartedAt().toString() : null)
                .build();
        return ApiResponse.success(response);
    }

    /**
     * 查询流程实例列表
     */
    @GetMapping("/process/list")
    @Operation(summary = "查询流程实例列表")
    public ApiResponse<List<ProcessInstanceDTO>> listProcessInstances(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        ProcessInstanceQuery query = ProcessInstanceQuery.builder()
                .tenantId(tenantId)
                .state(state)
                .page(page)
                .size(size)
                .build();
        List<ProcessInstanceDTO> list = workflowApplicationService.listProcessInstances(query);
        return ApiResponse.success(list);
    }

    /**
     * 挂起流程
     */
    @PostMapping("/process/{processInstanceId}/suspend")
    @Operation(summary = "挂起流程")
    public ApiResponse<Void> suspendProcess(
            @PathVariable String processInstanceId,
            @RequestParam String tenantId) {
        SuspendProcessCommand command = new SuspendProcessCommand(processInstanceId, tenantId);
        workflowApplicationService.suspendProcess(command);
        return ApiResponse.success("流程已挂起", null);
    }

    /**
     * 恢复流程
     */
    @PostMapping("/process/{processInstanceId}/resume")
    @Operation(summary = "恢复流程")
    public ApiResponse<Void> resumeProcess(
            @PathVariable String processInstanceId,
            @RequestParam String tenantId) {
        ResumeProcessCommand command = new ResumeProcessCommand(processInstanceId, tenantId);
        workflowApplicationService.resumeProcess(command);
        return ApiResponse.success("流程已恢复", null);
    }

    /**
     * 取消流程
     */
    @PostMapping("/process/{processInstanceId}/cancel")
    @Operation(summary = "取消流程")
    public ApiResponse<Void> cancelProcess(
            @PathVariable String processInstanceId,
            @RequestParam String tenantId,
            @RequestParam String reason) {
        CancelProcessCommand command = new CancelProcessCommand(processInstanceId, tenantId, reason);
        workflowApplicationService.cancelProcess(command);
        return ApiResponse.success("流程已取消", null);
    }
}
