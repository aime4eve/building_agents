package com.hkt.iot.order.domain.service;

import com.hkt.iot.order.domain.model.Money;
import com.hkt.iot.order.domain.model.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 订单定价服务
 * 领域服务，负责订单价格计算和折扣应用
 *
 * @author HKT IoT Team
 */
@Service
public class OrderPricingService {

    /**
     * 计算订单价格
     * 根据订单项计算总价格
     *
     * @param order 订单
     * @return 订单总金额
     */
    public Money calculateOrderPrice(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("订单不能为空");
        }
        order.recalculateTotalAmount();
        return Money.of(order.getTotalAmount());
    }

    /**
     * 应用折扣
     *
     * @param order 订单
     * @param discount 折扣信息
     * @return 折扣后的金额
     */
    public Money applyDiscount(Order order, Discount discount) {
        if (order == null) {
            throw new IllegalArgumentException("订单不能为空");
        }
        if (discount == null) {
            return Money.of(order.getTotalAmount());
        }

        BigDecimal totalAmount = order.getTotalAmount();
        BigDecimal discountAmount = discount.calculateDiscountAmount(totalAmount);

        if (discountAmount.compareTo(totalAmount) > 0) {
            throw new IllegalArgumentException("折扣金额不能大于订单总金额");
        }

        order.applyDiscount(discountAmount);
        return Money.of(order.getPayableAmount());
    }

    /**
     * 计算税费
     *
     * @param amount 金额
     * @param taxRate 税率
     * @return 税额
     */
    public Money calculateTax(Money amount, BigDecimal taxRate) {
        if (amount == null) {
            throw new IllegalArgumentException("金额不能为空");
        }
        if (taxRate == null || taxRate.compareTo(BigDecimal.ZERO) < 0) {
            taxRate = BigDecimal.ZERO;
        }
        return amount.multiply(taxRate.divide(BigDecimal.valueOf(100), 4, BigDecimal.ROUND_HALF_UP));
    }

    /**
     * 折扣信息
     */
    public static class Discount {
        private final DiscountType type;
        private final BigDecimal value;
        private final String description;

        public Discount(DiscountType type, BigDecimal value, String description) {
            this.type = type;
            this.value = value;
            this.description = description;
        }

        /**
         * 计算折扣金额
         */
        public BigDecimal calculateDiscountAmount(BigDecimal originalAmount) {
            if (type == DiscountType.PERCENTAGE) {
                return originalAmount.multiply(value)
                        .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            } else {
                return value;
            }
        }

        public DiscountType getType() {
            return type;
        }

        public BigDecimal getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 折扣类型
     */
    public enum DiscountType {
        PERCENTAGE("百分比折扣"),
        FIXED("固定金额折扣");

        private final String description;

        DiscountType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
