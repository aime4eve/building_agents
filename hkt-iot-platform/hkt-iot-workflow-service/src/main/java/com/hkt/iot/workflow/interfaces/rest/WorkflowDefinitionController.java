package com.hkt.iot.workflow.interfaces.rest;

import com.hkt.iot.workflow.application.command.ArchiveWorkflowDefinitionCommand;
import com.hkt.iot.workflow.application.command.CreateWorkflowDefinitionCommand;
import com.hkt.iot.workflow.application.command.PublishWorkflowDefinitionCommand;
import com.hkt.iot.workflow.application.dto.ApiResponse;
import com.hkt.iot.workflow.application.dto.WorkflowDefinitionDTO;
import com.hkt.iot.workflow.application.query.WorkflowDefinitionQuery;
import com.hkt.iot.workflow.application.service.WorkflowDefinitionApplicationService;
import com.hkt.iot.workflow.interfaces.dto.CreateWorkflowDefinitionRequest;
import com.hkt.iot.workflow.interfaces.dto.WorkflowDefinitionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程定义控制器
 */
@RestController
@RequestMapping("/api/v1/workflow-definitions")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "流程定义 API", description = "流程定义管理接口")
public class WorkflowDefinitionController {

    private final WorkflowDefinitionApplicationService workflowDefinitionApplicationService;

    @PostMapping
    @Operation(summary = "创建流程定义")
    public ApiResponse<WorkflowDefinitionResponse> createDefinition(
            @Valid @RequestBody CreateWorkflowDefinitionRequest request) {
        CreateWorkflowDefinitionCommand command = new CreateWorkflowDefinitionCommand(
                request.getTenantId(),
                request.getDefinitionKey(),
                request.getName(),
                request.getVersion(),
                request.getDescription(),
                request.getCreatedBy()
        );
        WorkflowDefinitionDTO dto = workflowDefinitionApplicationService.createDefinition(command);
        return ApiResponse.success(toResponse(dto));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布流程定义")
    public ApiResponse<WorkflowDefinitionResponse> publishDefinition(
            @PathVariable String id,
            @RequestParam String tenantId,
            @RequestParam String publishedBy) {
        PublishWorkflowDefinitionCommand command = new PublishWorkflowDefinitionCommand(
                id, tenantId, publishedBy
        );
        WorkflowDefinitionDTO dto = workflowDefinitionApplicationService.publishDefinition(command);
        return ApiResponse.success("流程定义已发布", toResponse(dto));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "归档流程定义")
    public ApiResponse<WorkflowDefinitionResponse> archiveDefinition(
            @PathVariable String id,
            @RequestParam String tenantId,
            @RequestParam String archivedBy) {
        ArchiveWorkflowDefinitionCommand command = new ArchiveWorkflowDefinitionCommand(
                id, tenantId, archivedBy
        );
        WorkflowDefinitionDTO dto = workflowDefinitionApplicationService.archiveDefinition(command);
        return ApiResponse.success("流程定义已归档", toResponse(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询流程定义")
    public ApiResponse<WorkflowDefinitionResponse> getDefinition(@PathVariable String id) {
        WorkflowDefinitionDTO dto = workflowDefinitionApplicationService.getDefinition(id);
        return ApiResponse.success(toResponse(dto));
    }

    @GetMapping("/key/{key}/latest")
    @Operation(summary = "获取最新版本")
    public ApiResponse<WorkflowDefinitionResponse> getLatestVersion(@PathVariable String key) {
        return workflowDefinitionApplicationService.getLatestVersion(key)
                .map(dto -> ApiResponse.success(toResponse(dto)))
                .orElse(ApiResponse.error("流程定义不存在: " + key));
    }

    @GetMapping
    @Operation(summary = "查询流程定义列表")
    public ApiResponse<List<WorkflowDefinitionResponse>> listDefinitions(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        WorkflowDefinitionQuery query = WorkflowDefinitionQuery.builder()
                .tenantId(tenantId)
                .status(status)
                .page(page)
                .size(size)
                .build();
        List<WorkflowDefinitionDTO> list = workflowDefinitionApplicationService.listDefinitions(query);
        List<WorkflowDefinitionResponse> responses = list.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(responses);
    }

    private WorkflowDefinitionResponse toResponse(WorkflowDefinitionDTO dto) {
        return WorkflowDefinitionResponse.builder()
                .definitionId(dto.getDefinitionId())
                .definitionKey(dto.getDefinitionKey())
                .name(dto.getName())
                .version(dto.getVersion())
                .status(dto.getStatus())
                .description(dto.getDescription())
                .createdAt(dto.getCreatedAt())
                .publishedAt(dto.getPublishedAt())
                .build();
    }
}
