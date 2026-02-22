package com.hkt.iot.order.application.service;

import com.hkt.iot.order.application.dto.EnergyBillDTO.*;
import com.hkt.iot.order.domain.model.*;
import com.hkt.iot.order.domain.repository.BillGenerationTaskRepository;
import com.hkt.iot.order.domain.repository.EnergyBillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 能耗账单应用服务
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnergyBillApplicationService {

    private final EnergyBillRepository energyBillRepository;
    private final BillGenerationTaskRepository taskRepository;

    /**
     * 创建能耗账单
     */
    @Transactional
    public BillResponse createBill(CreateBillRequest request, Long operatorId) {
        String billNo = generateBillNo();

        EnergyBill bill = EnergyBill.create(
                billNo,
                request.getTenantId(),
                request.getTenantCode(),
                request.getSpaceId(),
                request.getSpaceName(),
                EnergyType.valueOf(request.getEnergyType()),
                request.getBillingYear(),
                request.getBillingMonth(),
                request.getStartDate(),
                request.getEndDate(),
                request.getPreviousReading(),
                request.getCurrentReading(),
                request.getUnitPrice(),
                operatorId
        );

        if (request.getMeterId() != null) {
            bill.setMeterInfo(request.getMeterId(), request.getMeterNo());
        }
        if (request.getRemark() != null) {
            bill.setRemark(request.getRemark());
        }

        bill = energyBillRepository.save(bill);

        log.info("创建能耗账单成功: billNo={}, tenantId={}", billNo, request.getTenantId());
        return toBillResponse(bill);
    }

    /**
     * 更新能耗账单
     */
    @Transactional
    public BillResponse updateBill(Long billId, UpdateBillRequest request, Long operatorId) {
        EnergyBill bill = energyBillRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("账单不存在: " + billId));

        if (!bill.isEditable()) {
            throw new IllegalStateException("账单状态不允许编辑");
        }

        if (request.getPreviousReading() != null && request.getCurrentReading() != null) {
            bill.setReadings(request.getPreviousReading(), request.getCurrentReading());
        }
        if (request.getUnitPrice() != null) {
            bill.setUnitPrice(request.getUnitPrice());
        }
        if (request.getBaseCharge() != null) {
            bill.setBaseCharge(request.getBaseCharge());
        }
        if (request.getAdjustmentAmount() != null) {
            bill.setAdjustmentAmount(request.getAdjustmentAmount());
        }
        if (request.getRemark() != null) {
            bill.setRemark(request.getRemark());
        }

        bill = energyBillRepository.save(bill);

        log.info("更新能耗账单成功: billId={}", billId);
        return toBillResponse(bill);
    }

    /**
     * 提交账单确认
     */
    @Transactional
    public BillResponse submitForConfirmation(Long billId, Long operatorId) {
        EnergyBill bill = energyBillRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("账单不存在: " + billId));

        bill.submitForConfirmation(operatorId);
        bill = energyBillRepository.save(bill);

        log.info("提交账单确认成功: billId={}", billId);
        return toBillResponse(bill);
    }

    /**
     * 确认账单
     */
    @Transactional
    public BillResponse confirmBill(Long billId, Long operatorId) {
        EnergyBill bill = energyBillRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("账单不存在: " + billId));

        bill.confirm(operatorId);
        bill = energyBillRepository.save(bill);

        log.info("确认账单成功: billId={}", billId);
        return toBillResponse(bill);
    }

    /**
     * 取消账单
     */
    @Transactional
    public BillResponse cancelBill(Long billId, String reason, Long operatorId) {
        EnergyBill bill = energyBillRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("账单不存在: " + billId));

        bill.cancel(reason, operatorId);
        bill = energyBillRepository.save(bill);

        log.info("取消账单成功: billId={}, reason={}", billId, reason);
        return toBillResponse(bill);
    }

    /**
     * 生成月度账单
     */
    @Transactional
    public BillTaskResponse generateMonthlyBills(GenerateMonthlyBillsRequest request, Long operatorId) {
        String taskNo = generateTaskNo();

        BillGenerationTask task = BillGenerationTask.create(
                taskNo,
                request.getTenantId(),
                request.getBillingYear(),
                request.getBillingMonth(),
                request.getEnergyType() != null ? EnergyType.valueOf(request.getEnergyType()) : null,
                operatorId
        );

        task = taskRepository.save(task);

        log.info("创建账单生成任务成功: taskNo={}", taskNo);
        return toBillTaskResponse(task);
    }

    /**
     * 获取账单详情
     */
    public BillResponse getBill(Long billId) {
        EnergyBill bill = energyBillRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("账单不存在: " + billId));
        return toBillResponse(bill);
    }

    /**
     * 根据账单编号获取账单
     */
    public BillResponse getBillByNo(String billNo) {
        EnergyBill bill = energyBillRepository.findByBillNo(billNo)
                .orElseThrow(() -> new IllegalArgumentException("账单不存在: " + billNo));
        return toBillResponse(bill);
    }

    /**
     * 分页查询账单
     */
    public Page<BillResponse> searchBills(BillQueryRequest request, Pageable pageable) {
        return energyBillRepository.findAll(pageable).map(this::toBillResponse);
    }

    /**
     * 查询账单列表
     */
    public List<BillResponse> queryBills(Long tenantId, YearMonth from, YearMonth to) {
        List<EnergyBill> bills;
        if (from != null && to != null) {
            LocalDate startDate = from.atDay(1);
            LocalDate endDate = to.atEndOfMonth();
            bills = energyBillRepository.findByTenantIdAndStartDateBetween(tenantId, startDate, endDate);
        } else {
            bills = energyBillRepository.findByTenantId(tenantId);
        }
        return bills.stream()
                .map(this::toBillResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取租户账单列表
     */
    public List<BillResponse> getTenantBills(Long tenantId) {
        return energyBillRepository.findByTenantId(tenantId).stream()
                .map(this::toBillResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取账单生成任务
     */
    public BillTaskResponse getBillTask(Long taskId) {
        BillGenerationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在: " + taskId));
        return toBillTaskResponse(task);
    }

    /**
     * 获取账单统计
     */
    public BillStatisticsResponse getBillStatistics(Long tenantId, Integer year, Integer month) {
        List<EnergyBill> bills = energyBillRepository.findByTenantIdAndBillingYearAndBillingMonth(
                tenantId, year, month);

        long totalBills = bills.size();
        long pendingBills = bills.stream().filter(b -> b.getBillStatus() == BillStatus.PENDING).count();
        long confirmedBills = bills.stream().filter(b -> b.getBillStatus() == BillStatus.CONFIRMED).count();
        long paidBills = bills.stream().filter(b -> b.getBillStatus() == BillStatus.PAID).count();

        java.math.BigDecimal totalAmount = bills.stream()
                .map(EnergyBill::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal paidAmount = bills.stream()
                .filter(b -> b.getBillStatus() == BillStatus.PAID)
                .map(EnergyBill::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return BillStatisticsResponse.builder()
                .tenantId(tenantId)
                .billingYear(year)
                .billingMonth(month)
                .totalBills(totalBills)
                .pendingBills(pendingBills)
                .confirmedBills(confirmedBills)
                .paidBills(paidBills)
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .unpaidAmount(totalAmount.subtract(paidAmount))
                .build();
    }

    /**
     * 生成账单编号
     */
    private String generateBillNo() {
        return "BIL" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 生成任务编号
     */
    private String generateTaskNo() {
        return "TSK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 转换为账单响应
     */
    private BillResponse toBillResponse(EnergyBill bill) {
        List<BillItemResponse> itemResponses = bill.getItems().stream()
                .map(this::toBillItemResponse)
                .collect(Collectors.toList());

        return BillResponse.builder()
                .id(bill.getId())
                .billNo(bill.getBillNo())
                .tenantId(bill.getTenantId())
                .spaceId(bill.getSpaceId())
                .spaceName(bill.getSpaceName())
                .energyType(bill.getEnergyType().name())
                .billStatus(bill.getBillStatus().name())
                .billingYear(bill.getBillingYear())
                .billingMonth(bill.getBillingMonth())
                .startDate(bill.getStartDate())
                .endDate(bill.getEndDate())
                .previousReading(bill.getPreviousReading())
                .currentReading(bill.getCurrentReading())
                .usageAmount(bill.getUsageAmount())
                .unitPrice(bill.getUnitPrice())
                .baseCharge(bill.getBaseCharge())
                .adjustmentAmount(bill.getAdjustmentAmount())
                .totalAmount(bill.getTotalAmount())
                .currency(bill.getCurrency())
                .orderId(bill.getOrderId())
                .meterId(bill.getMeterId())
                .meterNo(bill.getMeterNo())
                .confirmedAt(bill.getConfirmedAt())
                .confirmedBy(bill.getConfirmedBy())
                .paidAt(bill.getPaidAt())
                .remark(bill.getRemark())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .items(itemResponses)
                .build();
    }

    /**
     * 转换为账单明细响应
     */
    private BillItemResponse toBillItemResponse(EnergyItem item) {
        return BillItemResponse.builder()
                .id(item.getId())
                .itemType(item.getItemType())
                .itemName(item.getItemName())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .unitPrice(item.getUnitPrice())
                .amount(item.getAmount())
                .description(item.getDescription())
                .sortOrder(item.getSortOrder())
                .build();
    }

    /**
     * 转换为任务响应
     */
    private BillTaskResponse toBillTaskResponse(BillGenerationTask task) {
        return BillTaskResponse.builder()
                .id(task.getId())
                .taskNo(task.getTaskNo())
                .tenantId(task.getTenantId())
                .billingYear(task.getBillingYear())
                .billingMonth(task.getBillingMonth())
                .energyType(task.getEnergyType() != null ? task.getEnergyType().name() : null)
                .taskStatus(task.getTaskStatus().name())
                .totalCount(task.getTotalCount())
                .successCount(task.getSuccessCount())
                .failedCount(task.getFailedCount())
                .progressPercentage(task.getProgressPercentage())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .errorMessage(task.getErrorMessage())
                .remark(task.getRemark())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
