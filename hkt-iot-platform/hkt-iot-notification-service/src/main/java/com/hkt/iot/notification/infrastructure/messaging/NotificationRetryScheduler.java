package com.hkt.iot.notification.infrastructure.messaging;

import com.hkt.iot.notification.application.service.NotificationApplicationService;
import com.hkt.iot.notification.domain.repository.NotificationLogRepository;
import com.hkt.iot.notification.domain.repository.NotificationRequestRepository;
import com.hkt.iot.notification.infrastructure.config.NotificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

    private final NotificationApplicationService notificationApplicationService;
    private final NotificationRequestRepository notificationRequestRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final NotificationProperties notificationProperties;

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
        int retentionDays = notificationProperties.getRetentionDays();
        log.info("开始清理过期通知数据，保留天数: {} 天", retentionDays);
        try {
            int deletedRequests = notificationRequestRepository.deleteExpiredRequests(retentionDays);
            log.info("清理过期通知请求完成，删除数量: {}", deletedRequests);
            
            int deletedLogs = notificationLogRepository.deleteExpiredLogs(retentionDays);
            log.info("清理过期通知日志完成，删除数量: {}", deletedLogs);
            
            log.info("清理过期通知数据完成，共删除请求: {}，日志: {}", deletedRequests, deletedLogs);
        } catch (Exception e) {
            log.error("清理过期数据异常", e);
        }
    }
}
