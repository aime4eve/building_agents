package com.hkt.iot.order.domain.model;

import com.hkt.iot.domain.model.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单聚合根
 * 管理订单生命周期和状态转换
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "tenant_code", nullable = false, length = 100)
    private String tenantCode;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "order_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Column(name = "order_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", precision = 18, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "refund_amount", precision = 18, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "subject", length = 200)
    private String subject;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "fulfilled_at")
    private LocalDateTime fulfilledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

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

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    /**
     * 工厂方法：创建订单
     */
    public static Order create(
            String orderNo,
            Long tenantId,
            String tenantCode,
            Long userId,
            OrderType orderType,
            String subject,
            String description,
            Long createdBy) {
        Order order = new Order();
        order.orderNo = orderNo;
        order.tenantId = tenantId;
        order.tenantCode = tenantCode;
        order.userId = userId;
        order.orderType = orderType;
        order.orderStatus = OrderStatus.PENDING;
        order.subject = subject;
        order.description = description;
        order.totalAmount = BigDecimal.ZERO;
        order.paidAmount = BigDecimal.ZERO;
        order.discountAmount = BigDecimal.ZERO;
        order.refundAmount = BigDecimal.ZERO;
        order.currency = "CNY";
        order.deleted = false;
        order.createdAt = LocalDateTime.now();
        order.updatedAt = LocalDateTime.now();
        order.createdBy = createdBy;
        order.updatedBy = createdBy;
        order.version = 0L;
        return order;
    }

    /**
     * 添加订单项
     */
    public void addItem(OrderItem item) {
        item.setOrder(this);
        this.items.add(item);
        recalculateTotalAmount();
    }

    /**
     * 移除订单项
     */
    public void removeItem(OrderItem item) {
        this.items.remove(item);
        item.setOrder(null);
        recalculateTotalAmount();
    }

    /**
     * 重新计算总金额
     */
    public void recalculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 支付成功
     */
    public void markAsPaid(BigDecimal paidAmount, Long operatorId) {
        if (!orderStatus.canPay()) {
            throw new IllegalStateException("当前订单状态不允许支付: " + orderStatus.getDescription());
        }
        this.orderStatus = OrderStatus.PAID;
        this.paidAmount = paidAmount;
        this.paidAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = operatorId;
    }

    /**
     * 履约完成
     */
    public void markAsFulfilled(Long operatorId) {
        if (!orderStatus.canFulfill()) {
            throw new IllegalStateException("当前订单状态不允许履约: " + orderStatus.getDescription());
        }
        this.orderStatus = OrderStatus.FULFILLED;
        this.fulfilledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = operatorId;
    }

    /**
     * 订单完成
     */
    public void markAsCompleted(Long operatorId) {
        if (!orderStatus.canComplete()) {
            throw new IllegalStateException("当前订单状态不允许完成: " + orderStatus.getDescription());
        }
        this.orderStatus = OrderStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = operatorId;
    }

    /**
     * 取消订单
     */
    public void cancel(String reason, Long operatorId) {
        if (!orderStatus.canCancel()) {
            throw new IllegalStateException("当前订单状态不允许取消: " + orderStatus.getDescription());
        }
        this.orderStatus = OrderStatus.CANCELLED;
        this.cancelReason = reason;
        this.cancelledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = operatorId;
    }

    /**
     * 退款
     */
    public void refund(BigDecimal refundAmount, String reason, Long operatorId) {
        if (!orderStatus.canRefund()) {
            throw new IllegalStateException("当前订单状态不允许退款: " + orderStatus.getDescription());
        }
        this.orderStatus = OrderStatus.REFUNDED;
        this.refundAmount = refundAmount;
        this.refundReason = reason;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = operatorId;
    }

    /**
     * 应用折扣
     */
    public void applyDiscount(BigDecimal discountAmount) {
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("折扣金额无效");
        }
        if (discountAmount.compareTo(this.totalAmount) > 0) {
            throw new IllegalArgumentException("折扣金额不能大于订单总金额");
        }
        this.discountAmount = discountAmount;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 获取应付金额
     */
    public BigDecimal getPayableAmount() {
        return totalAmount.subtract(discountAmount);
    }

    /**
     * 设置来源信息
     */
    public void setSource(String source, Long referenceId, String referenceType) {
        this.source = source;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
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
}
