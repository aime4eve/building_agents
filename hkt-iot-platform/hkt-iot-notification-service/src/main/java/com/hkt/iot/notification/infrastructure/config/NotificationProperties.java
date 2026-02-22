package com.hkt.iot.notification.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 通知服务配置属性
 *
 * @author HKT IoT Team
 */
@Data
@Component
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    private int maxRetry = 3;

    private String defaultPriority = "NORMAL";

    private int retentionDays = 30;

    private EmailConfig email = new EmailConfig();

    private SmsConfig sms = new SmsConfig();

    private PushConfig push = new PushConfig();

    private QueueConfig queue = new QueueConfig();

    @Data
    public static class EmailConfig {
        private boolean enabled = true;
        private String from = "noreply@hkt-iot.com";
    }

    @Data
    public static class SmsConfig {
        private boolean enabled = false;
        private String accessKeyId;
        private String accessKeySecret;
        private String signName = "华宽通智能体";
    }

    @Data
    public static class PushConfig {
        private boolean enabled = false;
        private String appKey;
        private String masterSecret;
    }

    @Data
    public static class QueueConfig {
        private String send = "notification.send.queue";
        private String retry = "notification.retry.queue";
        private String alarm = "notification.alarm.queue";
    }
}
