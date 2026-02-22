package com.hkt.iot.smartapps.moldprevention.interfaces.feignclient;

import com.hkt.iot.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 通知中心服务 Feign Client
 */
@FeignClient(name = "notification-service", contextId = "moldNotificationClient")
public interface NotificationFeignClient {

    /**
     * 发送通知
     */
    @PostMapping("/api/v1/notifications")
    Result<Void> sendNotification(@RequestBody Map<String, Object> notification);

    /**
     * 批量发送通知
     */
    @PostMapping("/api/v1/notifications/batch")
    Result<Void> sendBatchNotification(@RequestBody Map<String, Object> batchRequest);

    /**
     * 发送霉菌风险告警
     */
    @PostMapping("/api/v1/notifications/mold-alert")
    Result<Void> sendMoldAlert(@RequestBody Map<String, Object> alert);
}
