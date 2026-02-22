package com.hkt.iot.order.domain.service;

import com.hkt.iot.order.domain.model.*;
import com.hkt.iot.order.domain.repository.OrderRepository;
import com.hkt.iot.order.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 支付服务
 * 领域服务，负责支付相关业务逻辑
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /**
     * 发起支付
     *
     * @param orderId 订单ID
     * @param method 支付方式
     * @param operatorId 操作人ID
     * @return 支付记录
     */
    @Transactional
    public Payment initiatePayment(Long orderId, PaymentMethod method, Long operatorId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + orderId));

        if (!order.getOrderStatus().canPay()) {
            throw new IllegalStateException("订单状态不允许支付: " + order.getOrderStatus().getDescription());
        }

        String paymentNo = generatePaymentNo();
        BigDecimal payableAmount = order.getPayableAmount();

        Payment payment = Payment.create(
                paymentNo,
                order.getId(),
                order.getOrderNo(),
                order.getTenantId(),
                order.getUserId(),
                method,
                payableAmount,
                operatorId
        );

        payment.setExpireAt(LocalDateTime.now().plusHours(2));

        payment = paymentRepository.save(payment);

        log.info("发起支付成功: paymentNo={}, orderId={}, amount={}", paymentNo, orderId, payableAmount);
        return payment;
    }

    /**
     * 处理支付回调（幂等处理）
     *
     * @param callback 支付回调
     * @return 支付记录
     */
    @Transactional
    public Payment processPaymentCallback(PaymentCallback callback) {
        Payment payment = paymentRepository.findByPaymentNo(callback.getPaymentNo())
                .orElseThrow(() -> new IllegalArgumentException("支付记录不存在: " + callback.getPaymentNo()));

        if (payment.isSuccess()) {
            log.info("支付已成功，幂等处理: paymentNo={}", callback.getPaymentNo());
            return payment;
        }

        if (callback.isSuccess()) {
            payment.markAsSuccess(callback.getChannelTransactionNo(), callback.getCallbackData());

            Order order = orderRepository.findById(payment.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + payment.getOrderId()));
            order.markAsPaid(payment.getAmount(), callback.getOperatorId());
            orderRepository.save(order);

            log.info("支付成功: paymentNo={}, orderId={}", callback.getPaymentNo(), payment.getOrderId());
        } else {
            payment.markAsFailed(callback.getCallbackData());
            log.warn("支付失败: paymentNo={}, reason={}", callback.getPaymentNo(), callback.getFailReason());
        }

        return paymentRepository.save(payment);
    }

    /**
     * 退款
     *
     * @param paymentId 支付ID
     * @param refundAmount 退款金额
     * @param reason 退款原因
     * @param operatorId 操作人ID
     * @return 支付记录
     */
    @Transactional
    public Payment refundPayment(Long paymentId, BigDecimal refundAmount, String reason, Long operatorId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("支付记录不存在: " + paymentId));

        if (!payment.canRefund()) {
            throw new IllegalStateException("支付状态不允许退款: " + payment.getPaymentStatus().getDescription());
        }

        BigDecimal refundableAmount = payment.getRefundableAmount();
        if (refundAmount.compareTo(refundableAmount) > 0) {
            throw new IllegalArgumentException("退款金额超过可退款金额");
        }

        payment.refund(refundAmount);

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + payment.getOrderId()));
        order.refund(refundAmount, reason, operatorId);
        orderRepository.save(order);

        payment = paymentRepository.save(payment);

        log.info("退款成功: paymentId={}, refundAmount={}, reason={}", paymentId, refundAmount, reason);
        return payment;
    }

    /**
     * 生成支付编号
     */
    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 支付回调信息
     */
    public static class PaymentCallback {
        private final String paymentNo;
        private final String channelTransactionNo;
        private final String callbackData;
        private final boolean success;
        private final String failReason;
        private final Long operatorId;

        public PaymentCallback(String paymentNo, String channelTransactionNo, String callbackData,
                               boolean success, String failReason, Long operatorId) {
            this.paymentNo = paymentNo;
            this.channelTransactionNo = channelTransactionNo;
            this.callbackData = callbackData;
            this.success = success;
            this.failReason = failReason;
            this.operatorId = operatorId;
        }

        public String getPaymentNo() {
            return paymentNo;
        }

        public String getChannelTransactionNo() {
            return channelTransactionNo;
        }

        public String getCallbackData() {
            return callbackData;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getFailReason() {
            return failReason;
        }

        public Long getOperatorId() {
            return operatorId;
        }
    }
}
