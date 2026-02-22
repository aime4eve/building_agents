package com.hkt.iot.order.interfaces.rest;

import com.hkt.iot.order.application.dto.CommonResponse;
import com.hkt.iot.order.application.dto.PaymentCallbackRequest;
import com.hkt.iot.order.domain.model.Payment;
import com.hkt.iot.order.domain.repository.PaymentRepository;
import com.hkt.iot.order.domain.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 支付控制器
 *
 * @author HKT IoT Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "支付管理", description = "支付管理接口")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    /**
     * 支付回调（幂等处理）
     */
    @PostMapping("/callback")
    @Operation(summary = "支付回调", description = "处理支付渠道回调（幂等处理）")
    public CommonResponse<String> paymentCallback(@RequestBody PaymentCallbackRequest request) {
        log.info("收到支付回调: paymentNo={}, success={}", request.getPaymentNo(), request.isSuccess());

        PaymentService.PaymentCallback callback = new PaymentService.PaymentCallback(
                request.getPaymentNo(),
                request.getChannelTransactionNo(),
                request.getCallbackData(),
                request.isSuccess(),
                request.getFailReason(),
                1L
        );

        Payment payment = paymentService.processPaymentCallback(callback);

        log.info("支付回调处理完成: paymentNo={}, status={}", 
                request.getPaymentNo(), payment.getPaymentStatus());
        return CommonResponse.success("回调处理成功");
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/{paymentNo}")
    @Operation(summary = "查询支付状态", description = "根据支付编号查询支付状态")
    public CommonResponse<Payment> getPayment(@PathVariable String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new IllegalArgumentException("支付记录不存在: " + paymentNo));
        return CommonResponse.success(payment);
    }
}
