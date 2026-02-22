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
 * 能耗明细实体
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "energy_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EnergyItem extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter(AccessLevel.PACKAGE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private EnergyBill energyBill;

    @Column(name = "item_type", length = 50)
    private String itemType;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "quantity", precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "unit_price", precision = 18, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "amount", precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 工厂方法：创建能耗明细
     */
    public static EnergyItem create(
            String itemType,
            String itemName,
            BigDecimal quantity,
            String unit,
            BigDecimal unitPrice,
            Integer sortOrder) {
        EnergyItem item = new EnergyItem();
        item.itemType = itemType;
        item.itemName = itemName;
        item.quantity = quantity != null ? quantity : BigDecimal.ZERO;
        item.unit = unit;
        item.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        item.sortOrder = sortOrder != null ? sortOrder : 0;
        item.calculateAmount();
        item.createdAt = LocalDateTime.now();
        item.updatedAt = LocalDateTime.now();
        return item;
    }

    /**
     * 计算金额
     */
    public void calculateAmount() {
        if (quantity != null && unitPrice != null) {
            this.amount = quantity.multiply(unitPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.amount = BigDecimal.ZERO;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置数量
     */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        calculateAmount();
    }

    /**
     * 设置单价
     */
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateAmount();
    }

    /**
     * 设置描述
     */
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
}
