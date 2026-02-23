package com.hkt.iot.workflow.interfaces.rest;

import com.hkt.iot.workflow.application.command.AssignWorkOrderCommand;
import com.hkt.iot.workflow.application.command.CompleteWorkOrderCommand;
import com.hkt.iot.workflow.application.command.CreateWorkOrderCommand;
import com.hkt.iot.workflow.application.dto.ApiResponse;
import com.hkt.iot.workflow.application.dto.WorkOrderDTO;
import com.hkt.iot.workflow.application.query.WorkOrderQuery;
import com.hkt.iot.workflow.application.service.WorkOrderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工单控制器
 *
 * @author HKT IoT Team
 */
@RestController
@RequestMapping("/api/v1/work-orders")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "工单管理 API", description = "工单流转管理接口")
public class WorkOrderController {

    private final WorkOrderApplicationService workOrderApplicationService;

    @PostMapping
    @Operation(summary = "创建工单", description = "创建新的工单")
    public ApiResponse<WorkOrderDTO> createWorkOrder(
            @RequestBody CreateWorkOrderRequest request) {
        log.info("Creating work order: title={}, type={}", request.getTitle(), request.getType());
        
        CreateWorkOrderCommand command = CreateWorkOrderCommand.builder()
                .tenantId(request.getTenantId())
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .priority(request.getPriority())
                .templateId(request.getTemplateId())
                .spaceId(request.getSpaceId())
                .reporterId(request.getReporterId())
                .build();
        
        WorkOrderDTO dto = workOrderApplicationService.createWorkOrder(command);
        return ApiResponse.success(dto);
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "分配工单", description = "将工单分配给指定人员")
    public ApiResponse<WorkOrderDTO> assignWorkOrder(
            @Parameter(description = "工单ID") @PathVariable String id,
            @RequestBody AssignWorkOrderRequest request) {
        log.info("Assigning work order: id={}, assigneeId={}", id, request.getAssigneeId());
        
        AssignWorkOrderCommand command = AssignWorkOrderCommand.builder()
                .workOrderId(id)
                .assigneeId(request.getAssigneeId())
                .assignedBy(request.getAssignedBy())
                .build();
        
        WorkOrderDTO dto = workOrderApplicationService.assignWorkOrder(command);
        return ApiResponse.success(dto);
    }

    @PostMapping("/{id}/auto-assign")
    @Operation(summary = "自动派单", description = "根据规则自动分配工单")
    public ApiResponse<WorkOrderDTO> autoAssignWorkOrder(
            @Parameter(description = "工单ID") @PathVariable String id) {
        log.info("Auto assigning work order: id={}", id);
        
        WorkOrderDTO dto = workOrderApplicationService.autoAssignWorkOrder(id);
        return ApiResponse.success(dto);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "开始处理", description = "开始处理工单")
    public ApiResponse<WorkOrderDTO> startProcessing(
            @Parameter(description = "工单ID") @PathVariable String id,
            @RequestBody StartProcessingRequest request) {
        log.info("Starting work order processing: id={}, handlerId={}", id, request.getHandlerId());
        
        WorkOrderDTO dto = workOrderApplicationService.startProcessing(id, request.getHandlerId());
        return ApiResponse.success(dto);
    }

    @PostMapping("/{id}/submit-confirmation")
    @Operation(summary = "提交确认", description = "提交工单等待确认")
    public ApiResponse<WorkOrderDTO> submitForConfirmation(
            @Parameter(description = "工单ID") @PathVariable String id) {
        log.info("Submitting work order for confirmation: id={}", id);
        
        WorkOrderDTO dto = workOrderApplicationService.submitForConfirmation(id);
        return ApiResponse.success(dto);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "完成工单", description = "完成工单")
    public ApiResponse<WorkOrderDTO> completeWorkOrder(
            @Parameter(description = "工单ID") @PathVariable String id,
            @RequestBody CompleteWorkOrderRequest request) {
        log.info("Completing work order: id={}", id);
        
        CompleteWorkOrderCommand command = CompleteWorkOrderCommand.builder()
                .workOrderId(id)
                .handlerId(request.getHandlerId())
                .result(request.getResult())
                .build();
        
        WorkOrderDTO dto = workOrderApplicationService.completeWorkOrder(command);
        return ApiResponse.success(dto);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消工单", description = "取消工单")
    public ApiResponse<WorkOrderDTO> cancelWorkOrder(
            @Parameter(description = "工单ID") @PathVariable String id,
            @RequestBody CancelWorkOrderRequest request) {
        log.info("Cancelling work order: id={}", id);
        
        WorkOrderDTO dto = workOrderApplicationService.cancelWorkOrder(
                id, 
                request.getCancelledBy(), 
                request.getReason()
        );
        return ApiResponse.success(dto);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "驳回工单", description = "驳回工单")
    public ApiResponse<WorkOrderDTO> rejectWorkOrder(
            @Parameter(description = "工单ID") @PathVariable String id,
            @RequestBody RejectWorkOrderRequest request) {
        log.info("Rejecting work order: id={}", id);
        
        WorkOrderDTO dto = workOrderApplicationService.rejectWorkOrder(id, request.getReason());
        return ApiResponse.success(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询工单", description = "根据ID查询工单详情")
    public ApiResponse<WorkOrderDTO> getWorkOrder(
            @Parameter(description = "工单ID") @PathVariable String id) {
        log.info("Getting work order: id={}", id);
        
        WorkOrderDTO dto = workOrderApplicationService.getWorkOrder(id);
        return ApiResponse.success(dto);
    }

    @GetMapping("/no/{workOrderNo}")
    @Operation(summary = "根据工单编号查询", description = "根据工单编号查询工单详情")
    public ApiResponse<WorkOrderDTO> getWorkOrderByNo(
            @Parameter(description = "工单编号") @PathVariable String workOrderNo) {
        log.info("Getting work order by no: workOrderNo={}", workOrderNo);
        
        WorkOrderDTO dto = workOrderApplicationService.getWorkOrderByNo(workOrderNo);
        return ApiResponse.success(dto);
    }

    @GetMapping
    @Operation(summary = "列表查询", description = "查询工单列表")
    public ApiResponse<List<WorkOrderDTO>> listWorkOrders(
            @Parameter(description = "租户ID") @RequestParam(required = false) String tenantId,
            @Parameter(description = "工单编号") @RequestParam(required = false) String workOrderNo,
            @Parameter(description = "标题") @RequestParam(required = false) String title,
            @Parameter(description = "工单类型") @RequestParam(required = false) String type,
            @Parameter(description = "工单状态") @RequestParam(required = false) String status,
            @Parameter(description = "优先级") @RequestParam(required = false) String priority,
            @Parameter(description = "指派人ID") @RequestParam(required = false) String assigneeId,
            @Parameter(description = "报告人ID") @RequestParam(required = false) String reporterId,
            @Parameter(description = "空间ID") @RequestParam(required = false) String spaceId,
            @Parameter(description = "是否超时") @RequestParam(required = false) Boolean overdue,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size) {
        log.info("Listing work orders: tenantId={}, status={}, assigneeId={}", tenantId, status, assigneeId);
        
        WorkOrderQuery query = WorkOrderQuery.builder()
                .tenantId(tenantId)
                .workOrderNo(workOrderNo)
                .title(title)
                .type(type)
                .status(status)
                .priority(priority)
                .assigneeId(assigneeId)
                .reporterId(reporterId)
                .spaceId(spaceId)
                .overdue(overdue)
                .page(page)
                .size(size)
                .build();
        
        List<WorkOrderDTO> list = workOrderApplicationService.listWorkOrders(query);
        return ApiResponse.success(list);
    }

    @GetMapping("/overdue")
    @Operation(summary = "查询超时工单", description = "查询所有超时未完成的工单")
    public ApiResponse<List<WorkOrderDTO>> listOverdueWorkOrders() {
        log.info("Listing overdue work orders");
        
        List<WorkOrderDTO> list = workOrderApplicationService.listOverdueWorkOrders();
        return ApiResponse.success(list);
    }

    @lombok.Data
    public static class CreateWorkOrderRequest {
        String tenantId;
        String title;
        String description;
        String type;
        String priority;
        String templateId;
        String spaceId;
        String reporterId;
    }

    @lombok.Data
    public static class AssignWorkOrderRequest {
        String assigneeId;
        String assignedBy;
    }

    @lombok.Data
    public static class StartProcessingRequest {
        String handlerId;
    }

    @lombok.Data
    public static class CompleteWorkOrderRequest {
        String handlerId;
        String result;
    }

    @lombok.Data
    public static class CancelWorkOrderRequest {
        String cancelledBy;
        String reason;
    }

    @lombok.Data
    public static class RejectWorkOrderRequest {
        String reason;
    }
}
