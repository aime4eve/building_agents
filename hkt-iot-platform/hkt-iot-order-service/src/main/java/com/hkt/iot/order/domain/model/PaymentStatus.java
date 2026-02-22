package com.hkt.iot.order.domain.model;

/**
 * 支付状态枚举
 *
 * @author HKT IoT Team
 */
public enum PaymentStatus {

    PENDING("待支付"),

    PROCESSING("处理中"),

    SUCCESS("支付成功"),

    FAILED("支付失败"),

    REFUNDED("已退款"),

    PARTIAL_REFUNDED("部分退款");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
