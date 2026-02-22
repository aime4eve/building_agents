package com.hkt.iot.order.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.order.domain.model.Payment;
import com.hkt.iot.order.domain.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 支付仓储接口
 *
 * @author HKT IoT Team
 */
public interface PaymentRepository extends BaseRepository<Payment, Long> {

    /**
     * 根据支付编号查找
     */
    Optional<Payment> findByPaymentNo(String paymentNo);

    /**
     * 根据订单ID查找支付记录
     */
    List<Payment> findByOrderId(Long orderId);

    /**
     * 根据订单ID查找成功的支付记录
     */
    Optional<Payment> findSuccessPaymentByOrderId(Long orderId);

    /**
     * 根据租户ID查找支付记录
     */
    List<Payment> findByTenantId(Long tenantId);

    /**
     * 根据用户ID查找支付记录
     */
    List<Payment> findByUserId(Long userId);

    /**
     * 根据渠道交易号查找
     */
    Optional<Payment> findByChannelTransactionNo(String channelTransactionNo);

    /**
     * 根据渠道订单号查找
     */
    Optional<Payment> findByChannelOrderNo(String channelOrderNo);

    /**
     * 检查支付编号是否存在
     */
    boolean existsByPaymentNo(String paymentNo);

    /**
     * 查找指定状态的支付记录
     */
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

    /**
     * 查找过期的待支付记录
     */
    List<Payment> findExpiredPendingPayments(LocalDateTime expireTime);

    /**
     * 统计订单支付总额
     */
    Long sumSuccessAmountByOrderId(Long orderId);
}
