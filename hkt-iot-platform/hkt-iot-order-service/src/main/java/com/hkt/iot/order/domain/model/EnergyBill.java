package com.hkt.iot.order.domain.model;

import com.hkt.iot.domain.model.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * 能耗账单聚合根
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "energy_bill")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnergyBill extends AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_no", nullable = false, unique = true, length = 50)
    private String billNo;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "tenant_code", nullable = false, length = 100)
    private String tenantCode;

    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "space_name", length = 200)
    private String spaceName;

    @Column(name = "energy_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EnergyType energyType;

    @Column(name = "bill_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BillStatus billStatus;

    @Column(name = "billing_year", nullable = false)
    private Integer billingYear;

    @Column(name = "billing_month", nullable = false)
    private Integer billingMonth;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "previous_reading", precision = 18, scale = 4)
    private BigDecimal previousReading;

    @Column(name = "current_reading", precision = 18, scale = 4)
    private BigDecimal currentReading;

    @Column(name = "usage_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal usageAmount;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "base_charge", precision = 18, scale = 2)
    private BigDecimal baseCharge;

    @Column(name = "adjustment_amount", precision = 18, scale = 2)
    private BigDecimal adjustmentAmount;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "meter_id")
    private Long meterId;

    @Column(name = "meter_no", length = 50)
    private String meterNo;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "confirmed_by")
    private Long confirmedBy;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "remark", length = 500)
    private String remark;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @OneToMany(mappedBy = "energyBill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EnergyItem> items = new ArrayList<>();

    /**
     * 工厂方法：创建能耗账单
     */
    public static EnergyBill create(
            String billNo,
            Long tenantId,
            String tenantCode,
            Long spaceId,
            String spaceName,
            EnergyType energyType,
            Integer billingYear,
            Integer billingMonth,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal previousReading,
            BigDecimal currentReading,
            BigDecimal unitPrice,
            Long createdBy) {
        EnergyBill bill = new EnergyBill();
        bill.billNo = billNo;
        bill.tenantId = tenantId;
        bill.tenantCode = tenantCode;
        bill.spaceId = spaceId;
        bill.spaceName = spaceName;
        bill.energyType = energyType;
        bill.billStatus = BillStatus.DRAFT;
        bill.billingYear = billingYear;
        bill.billingMonth = billingMonth;
        bill.startDate = startDate;
        bill.endDate = endDate;
        bill.previousReading = previousReading != null ? previousReading : BigDecimal.ZERO;
        bill.currentReading = currentReading != null ? currentReading : BigDecimal.ZERO;
        bill.usageAmount = bill.currentReading.subtract(bill.previousReading);
        bill.unitPrice = unitPrice;
        bill.baseCharge = BigDecimal.ZERO;
        bill.adjustmentAmount = BigDecimal.ZERO;
        bill.totalAmount = bill.calculateTotalAmount();
        bill.currency = "CNY";
        bill.deleted = false;
        bill.createdAt = LocalDateTime.now();
        bill.updatedAt = LocalDateTime.now();
        bill.createdBy = createdBy;
        bill.updatedBy = createdBy;
        bill.version = 0L;
        return bill;
    }

    /**
     * 计算总金额
     */
    private BigDecimal calculateTotalAmount() {
        BigDecimal usageCost = usageAmount.multiply(unitPrice);
        BigDecimal total = usageCost;
        if (baseCharge != null) {
            total = total.add(baseCharge);
        }
        if (adjustmentAmount != null) {
            total = total.add(adjustmentAmount);
        }
        return total.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 添加能耗明细
     */
    public void addItem(EnergyItem item) {
        item.setEnergyBill(this);
        this.items.add(item);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置抄表读数
     */
    public void setReadings(BigDecimal previousReading, BigDecimal currentReading) {
        if (currentReading.compareTo(previousReading) < 0) {
            throw new IllegalArgumentException("当前读数不能小于上期读数");
        }
        this.previousReading = previousReading;
        this.currentReading = currentReading;
        this.usageAmount = currentReading.subtract(previousReading);
        this.totalAmount = calculateTotalAmount();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置单价
     */
    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("单价无效");
        }
        this.unitPrice = unitPrice;
        this.totalAmount = calculateTotalAmount();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置基础费用
     */
    public void setBaseCharge(BigDecimal baseCharge) {
        this.baseCharge = baseCharge != null ? baseCharge : BigDecimal.ZERO;
        this.totalAmount = calculateTotalAmount();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置调整金额
     */
    public void setAdjustmentAmount(BigDecimal adjustmentAmount) {
        this.adjustmentAmount = adjustmentAmount != null ? adjustmentAmount : BigDecimal.ZERO;
        this.totalAmount = calculateTotalAmount();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 提交确认
     */
    public void submitForConfirmation(Long operatorId) {
        if (this.billStatus != BillStatus.DRAFT) {
            throw new IllegalStateException("只有草稿状态的账单可以提交确认");
        }
        this.billStatus = BillStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = operatorId;
    }

    /**
     * 确认账单
     */
    public void confirm(Long operatorId) {
        if (this.billStatus != BillStatus.PENDING) {
            throw new IllegalStateException("只有待确认状态的账单可以确认");
        }
        this.billStatus = BillStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.confirmedBy = operatorId;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = operatorId;
    }

    /**
     * 标记已支付
     */
    public void markAsPaid(Long orderId) {
        if (this.billStatus != BillStatus.CONFIRMED) {
            throw new IllegalStateException("只有已确认状态的账单可以标记已支付");
        }
        this.billStatus = BillStatus.PAID;
        this.orderId = orderId;
        this.paidAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 取消账单
     */
    public void cancel(String reason, Long operatorId) {
        if (this.billStatus == BillStatus.PAID) {
            throw new IllegalStateException("已支付的账单不能取消");
        }
        this.billStatus = BillStatus.CANCELLED;
        this.remark = reason;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = operatorId;
    }

    /**
     * 设置关联订单
     */
    public void setOrder(Long orderId) {
        this.orderId = orderId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置表计信息
     */
    public void setMeterInfo(Long meterId, String meterNo) {
        this.meterId = meterId;
        this.meterNo = meterNo;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 软删除
     */
    public void softDelete(Long deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 获取账单周期
     */
    public YearMonth getBillingPeriod() {
        return YearMonth.of(billingYear, billingMonth);
    }

    /**
     * 是否可编辑
     */
    public boolean isEditable() {
        return this.billStatus == BillStatus.DRAFT;
    }

    /**
     * 是否可确认
     */
    public boolean isConfirmable() {
        return this.billStatus == BillStatus.PENDING;
    }

    /**
     * 是否可支付
     */
    public boolean isPayable() {
        return this.billStatus == BillStatus.CONFIRMED;
    }
}
