package com.hkt.iot.order.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项实体
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter(AccessLevel.PACKAGE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_code", length = 100)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "product_type", length = 50)
    private String productType;

    @Column(name = "specification", length = 500)
    private String specification;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "subtotal", nullable = false, precision = 18, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 工厂方法：创建订单项
     */
    public static OrderItem create(
            Long productId,
            String productCode,
            String productName,
            String productType,
            String specification,
            String unit,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal discountRate) {
        OrderItem item = new OrderItem();
        item.productId = productId;
        item.productCode = productCode;
        item.productName = productName;
        item.productType = productType;
        item.specification = specification;
        item.unit = unit;
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        item.discountRate = discountRate != null ? discountRate : BigDecimal.ZERO;
        item.discountAmount = BigDecimal.ZERO;
        item.createdAt = LocalDateTime.now();
        item.updatedAt = LocalDateTime.now();
        item.calculateSubtotal();
        return item;
    }

    /**
     * 计算小计
     */
    public void calculateSubtotal() {
        BigDecimal baseAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = baseAmount.multiply(discountRate).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        }
        this.subtotal = baseAmount.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新数量
     */
    public void updateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("数量无效");
        }
        this.quantity = quantity;
        calculateSubtotal();
    }

    /**
     * 更新单价
     */
    public void updateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("单价无效");
        }
        this.unitPrice = unitPrice;
        calculateSubtotal();
    }

    /**
     * 设置备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
        this.updatedAt = LocalDateTime.now();
    }
}
