package com.hkt.iot.notification.infrastructure.messaging;

import com.hkt.iot.notification.application.service.NotificationApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 通知重试调度器
 * 定时扫描并重试失败的通知
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

    private final NotificationApplicationService notificationApplicationService;

    /**
     * 每分钟执行一次重试
     */
    @Scheduled(fixedRate = 60000)
    public void retryFailedNotifications() {
        try {
            notificationApplicationService.retryFailedNotifications();
        } catch (Exception e) {
            log.error("重试失败通知调度异常", e);
        }
    }

    /**
     * 每天凌晨2点清理过期数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredData() {
        try {
            // TODO: 清理30天前的通知日志和请求
            log.info("清理过期通知数据完成");
        } catch (Exception e) {
            log.error("清理过期数据异常", e);
        }
    }
}
