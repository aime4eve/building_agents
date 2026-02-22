package com.hkt.iot.order.interfaces.rest;

import com.hkt.iot.order.application.dto.CommonResponse;
import com.hkt.iot.order.application.dto.OrderDTO.*;
import com.hkt.iot.order.application.dto.PageResponse;
import com.hkt.iot.order.application.service.OrderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 *
 * @author HKT IoT Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单管理接口")
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    /**
     * 创建订单
     */
    @PostMapping
    @Operation(summary = "创建订单", description = "创建新订单")
    public CommonResponse<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long operatorId = 1L;
        OrderResponse response = orderApplicationService.createOrder(request, operatorId);
        return CommonResponse.success(response);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情", description = "根据ID获取订单详细信息")
    public CommonResponse<OrderResponse> getOrder(@PathVariable Long orderId) {
        OrderResponse response = orderApplicationService.getOrder(orderId);
        return CommonResponse.success(response);
    }

    /**
     * 根据订单编号获取订单
     */
    @GetMapping("/no/{orderNo}")
    @Operation(summary = "根据订单编号获取订单", description = "根据订单编号获取订单信息")
    public CommonResponse<OrderResponse> getOrderByNo(@PathVariable String orderNo) {
        OrderResponse response = orderApplicationService.getOrderByNo(orderNo);
        return CommonResponse.success(response);
    }

    /**
     * 分页查询订单
     */
    @PostMapping("/search")
    @Operation(summary = "分页查询订单", description = "根据条件分页查询订单列表")
    public CommonResponse<PageResponse<OrderResponse>> searchOrders(
            @RequestBody OrderQueryRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var page = orderApplicationService.searchOrders(request, pageable);
        return CommonResponse.success(PageResponse.of(page));
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户订单列表", description = "获取指定用户的所有订单")
    public CommonResponse<List<OrderResponse>> getUserOrders(@PathVariable Long userId) {
        List<OrderResponse> response = orderApplicationService.getUserOrders(userId);
        return CommonResponse.success(response);
    }

    /**
     * 获取租户订单列表
     */
    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "获取租户订单列表", description = "获取指定租户的所有订单")
    public CommonResponse<List<OrderResponse>> getTenantOrders(@PathVariable Long tenantId) {
        List<OrderResponse> response = orderApplicationService.getTenantOrders(tenantId);
        return CommonResponse.success(response);
    }

    /**
     * 支付订单
     */
    @PostMapping("/{orderId}/pay")
    @Operation(summary = "支付订单", description = "发起订单支付")
    public CommonResponse<PaymentResponse> payOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody PayOrderRequest request) {
        Long operatorId = 1L;
        PaymentResponse response = orderApplicationService.payOrder(orderId, request, operatorId);
        return CommonResponse.success(response);
    }

    /**
     * 履约订单
     */
    @PostMapping("/{orderId}/fulfill")
    @Operation(summary = "履约订单", description = "标记订单为已履约")
    public CommonResponse<OrderResponse> fulfillOrder(@PathVariable Long orderId) {
        Long operatorId = 1L;
        OrderResponse response = orderApplicationService.fulfillOrder(orderId, operatorId);
        return CommonResponse.success(response);
    }

    /**
     * 完成订单
     */
    @PostMapping("/{orderId}/complete")
    @Operation(summary = "完成订单", description = "标记订单为已完成")
    public CommonResponse<OrderResponse> completeOrder(@PathVariable Long orderId) {
        Long operatorId = 1L;
        OrderResponse response = orderApplicationService.completeOrder(orderId, operatorId);
        return CommonResponse.success(response);
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "取消订单", description = "取消订单")
    public CommonResponse<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequest request) {
        Long operatorId = 1L;
        OrderResponse response = orderApplicationService.cancelOrder(orderId, request, operatorId);
        return CommonResponse.success(response);
    }

    /**
     * 退款订单
     */
    @PostMapping("/{orderId}/refund")
    @Operation(summary = "退款订单", description = "订单退款")
    public CommonResponse<OrderResponse> refundOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody RefundOrderRequest request) {
        Long operatorId = 1L;
        OrderResponse response = orderApplicationService.refundOrder(orderId, request, operatorId);
        return CommonResponse.success(response);
    }
}
