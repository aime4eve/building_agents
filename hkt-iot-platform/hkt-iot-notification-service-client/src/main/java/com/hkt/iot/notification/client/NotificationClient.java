package com.hkt.iot.notification.client;

import com.hkt.iot.notification.client.dto.NotificationResponse;
import com.hkt.iot.notification.client.dto.NotificationSendRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * 通知服务Feign客户端
 *
 * @author HKT IoT Team
 */
@FeignClient(name = "hkt-iot-notification-service", contextId = "notificationClient")
public interface NotificationClient {

    /**
     * 发送通知
     *
     * @param request  通知发送请求
     * @param tenantId 租户ID
     * @return 通知响应
     */
    @PostMapping("/api/v1/notifications/send")
    NotificationResponse sendNotification(
            @RequestBody NotificationSendRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId
    );

    /**
     * 批量发送通知
     *
     * @param requests 通知发送请求列表
     * @param tenantId 租户ID
     * @return 通知响应列表
     */
    @PostMapping("/api/v1/notifications/batch-send")
    List<NotificationResponse> batchSendNotifications(
            @RequestBody List<NotificationSendRequest> requests,
            @RequestHeader("X-Tenant-Id") String tenantId
    );

    /**
     * 获取请求状态
     *
     * @param requestId 请求ID
     * @param tenantId  租户ID
     * @return 通知响应
     */
    @GetMapping("/api/v1/notifications/requests/{requestId}")
    NotificationResponse getRequestStatus(
            @PathVariable("requestId") Long requestId,
            @RequestHeader("X-Tenant-Id") String tenantId
    );

    /**
     * 取消通知
     *
     * @param requestId 请求ID
     * @param tenantId  租户ID
     * @return 通知响应
     */
    @PostMapping("/api/v1/notifications/requests/{requestId}/cancel")
    NotificationResponse cancelNotification(
            @PathVariable("requestId") Long requestId,
            @RequestHeader("X-Tenant-Id") String tenantId
    );
}
