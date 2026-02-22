package com.hkt.iot.order.interfaces.rest;

import com.hkt.iot.order.application.dto.CommonResponse;
import com.hkt.iot.order.application.dto.EnergyBillDTO.*;
import com.hkt.iot.order.application.dto.PageResponse;
import com.hkt.iot.order.application.service.EnergyBillApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

/**
 * 能耗账单控制器
 *
 * @author HKT IoT Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/energy-bills")
@RequiredArgsConstructor
@Tag(name = "能耗账单管理", description = "能耗账单管理接口")
public class EnergyBillController {

    private final EnergyBillApplicationService billApplicationService;

    /**
     * 创建能耗账单
     */
    @PostMapping
    @Operation(summary = "创建能耗账单", description = "创建新的能耗账单")
    public CommonResponse<BillResponse> createBill(@Valid @RequestBody CreateBillRequest request) {
        Long operatorId = 1L;
        BillResponse response = billApplicationService.createBill(request, operatorId);
        return CommonResponse.success(response);
    }

    /**
     * 更新能耗账单
     */
    @PutMapping("/{billId}")
    @Operation(summary = "更新能耗账单", description = "更新能耗账单信息")
    public CommonResponse<BillResponse> updateBill(
            @PathVariable Long billId,
            @Valid @RequestBody UpdateBillRequest request) {
        Long operatorId = 1L;
        BillResponse response = billApplicationService.updateBill(billId, request, operatorId);
        return CommonResponse.success(response);
    }

    /**
     * 获取账单详情
     */
    @GetMapping("/{billId}")
    @Operation(summary = "获取账单详情", description = "根据ID获取账单详细信息")
    public CommonResponse<BillResponse> getBill(@PathVariable Long billId) {
        BillResponse response = billApplicationService.getBill(billId);
        return CommonResponse.success(response);
    }

    /**
     * 根据账单编号获取账单
     */
    @GetMapping("/no/{billNo}")
    @Operation(summary = "根据账单编号获取账单", description = "根据账单编号获取账单信息")
    public CommonResponse<BillResponse> getBillByNo(@PathVariable String billNo) {
        BillResponse response = billApplicationService.getBillByNo(billNo);
        return CommonResponse.success(response);
    }

    /**
     * 分页查询账单
     */
    @PostMapping("/search")
    @Operation(summary = "分页查询账单", description = "根据条件分页查询账单列表")
    public CommonResponse<PageResponse<BillResponse>> searchBills(
            @RequestBody BillQueryRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var page = billApplicationService.searchBills(request, pageable);
        return CommonResponse.success(PageResponse.of(page));
    }

    /**
     * 查询账单列表
     */
    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "查询租户账单", description = "查询指定租户的账单列表")
    public CommonResponse<List<BillResponse>> queryBills(
            @PathVariable Long tenantId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth to) {
        List<BillResponse> response = billApplicationService.queryBills(tenantId, from, to);
        return CommonResponse.success(response);
    }

    /**
     * 提交账单确认
     */
    @PostMapping("/{billId}/submit")
    @Operation(summary = "提交账单确认", description = "提交账单等待确认")
    public CommonResponse<BillResponse> submitForConfirmation(@PathVariable Long billId) {
        Long operatorId = 1L;
        BillResponse response = billApplicationService.submitForConfirmation(billId, operatorId);
        return CommonResponse.success(response);
    }

    /**
     * 确认账单
     */
    @PostMapping("/{billId}/confirm")
    @Operation(summary = "确认账单", description = "确认账单")
    public CommonResponse<BillResponse> confirmBill(@PathVariable Long billId) {
        Long operatorId = 1L;
        BillResponse response = billApplicationService.confirmBill(billId, operatorId);
        return CommonResponse.success(response);
    }

    /**
     * 取消账单
     */
    @PostMapping("/{billId}/cancel")
    @Operation(summary = "取消账单", description = "取消账单")
    public CommonResponse<BillResponse> cancelBill(
            @PathVariable Long billId,
            @RequestParam String reason) {
        Long operatorId = 1L;
        BillResponse response = billApplicationService.cancelBill(billId, reason, operatorId);
        return CommonResponse.success(response);
    }

    /**
     * 生成月度账单
     */
    @PostMapping("/generate")
    @Operation(summary = "生成月度账单", description = "批量生成月度能耗账单")
    public CommonResponse<BillTaskResponse> generateMonthlyBills(
            @Valid @RequestBody GenerateMonthlyBillsRequest request) {
        Long operatorId = 1L;
        BillTaskResponse response = billApplicationService.generateMonthlyBills(request, operatorId);
        return CommonResponse.success(response);
    }

    /**
     * 获取账单生成任务
     */
    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "获取账单生成任务", description = "获取账单生成任务详情")
    public CommonResponse<BillTaskResponse> getBillTask(@PathVariable Long taskId) {
        BillTaskResponse response = billApplicationService.getBillTask(taskId);
        return CommonResponse.success(response);
    }

    /**
     * 获取账单统计
     */
    @GetMapping("/statistics/{tenantId}")
    @Operation(summary = "获取账单统计", description = "获取指定租户的账单统计信息")
    public CommonResponse<BillStatisticsResponse> getBillStatistics(
            @PathVariable Long tenantId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        BillStatisticsResponse response = billApplicationService.getBillStatistics(tenantId, year, month);
        return CommonResponse.success(response);
    }
}
