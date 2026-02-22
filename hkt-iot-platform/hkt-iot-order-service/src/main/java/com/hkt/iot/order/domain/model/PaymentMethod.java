package com.hkt.iot.order.domain.model;

/**
 * 支付方式枚举
 *
 * @author HKT IoT Team
 */
public enum PaymentMethod {

    ALIPAY("支付宝"),

    WECHAT("微信支付"),

    BANK_TRANSFER("银行转账"),

    CREDIT_CARD("信用卡"),

    BALANCE("余额支付");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
