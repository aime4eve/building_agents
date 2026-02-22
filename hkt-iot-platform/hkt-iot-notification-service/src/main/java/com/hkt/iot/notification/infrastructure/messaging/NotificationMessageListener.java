package com.hkt.iot.notification.infrastructure.messaging;

import com.hkt.iot.notification.application.service.AlarmNotificationService;
import com.hkt.iot.notification.application.service.NotificationApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 通知消息监听器
 * 处理通知发送和重试消息
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageListener {

    private final NotificationApplicationService notificationApplicationService;
    private final AlarmNotificationService alarmNotificationService;

    /**
     * 监听通知发送消息
     */
    @RabbitListener(queues = "${notification.queue.send:notification.send.queue}")
    public void handleSendNotification(Long requestId) {
        try {
            log.info("收到通知发送消息: {}", requestId);
            notificationApplicationService.processNotification(requestId);
        } catch (Exception e) {
            log.error("处理通知发送消息失败: {}", requestId, e);
        }
    }

    /**
     * 监听通知重试消息
     */
    @RabbitListener(queues = "${notification.queue.retry:notification.retry.queue}")
    public void handleRetryNotification(Long requestId) {
        try {
            log.info("收到通知重试消息: {}", requestId);
            notificationApplicationService.processNotification(requestId);
        } catch (Exception e) {
            log.error("处理通知重试消息失败: {}", requestId, e);
        }
    }

    /**
     * 监听告警触发事件
     */
    @RabbitListener(queues = "${notification.queue.alarm:notification.alarm.queue}")
    public void handleAlarmTriggered(String event) {
        try {
            log.info("收到告警触发事件: {}", event);
            alarmNotificationService.handleAlarmTriggered(event);
        } catch (Exception e) {
            log.error("处理告警触发事件失败", e);
        }
    }
}
