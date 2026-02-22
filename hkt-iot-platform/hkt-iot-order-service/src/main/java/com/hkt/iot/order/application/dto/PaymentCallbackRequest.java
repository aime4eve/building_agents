package com.hkt.iot.order.application.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付回调请求
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class PaymentCallbackRequest {

    /**
     * 支付编号
     */
    private String paymentNo;

    /**
     * 渠道交易号
     */
    private String channelTransactionNo;

    /**
     * 渠道订单号
     */
    private String channelOrderNo;

    /**
     * 回调数据
     */
    private String callbackData;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 支付金额
     */
    private BigDecimal paidAmount;

    /**
     * 支付时间戳
     */
    private Long paidTimestamp;
}
