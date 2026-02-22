package com.hkt.iot.smartapps.smartlivestock.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 健康告警值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthAlert {

    private String id;
    private LivestockId livestockId;
    private AlertType type;
    private AlertSeverity severity;
    private String message;
    private LocalDateTime triggeredAt;
    private LocalDateTime resolvedAt;
    private boolean resolved;

    /**
     * 告警类型枚举
     */
    public enum AlertType {
        /**
         * 体温异常
         */
        TEMPERATURE_ABNORMAL,

        /**
         * 活动量不足
         */
        LOW_ACTIVITY,

        /**
         * 健康评分下降
         */
        HEALTH_SCORE_DECLINE,

        /**
         * 越界告警
         */
        GEOFENCE_VIOLATION,

        /**
         * 饲料摄入异常
         */
        FEED_INTAKE_ABNORMAL
    }

    /**
     * 告警严重程度枚举
     */
    public enum AlertSeverity {
        /**
         * 信息
         */
        INFO,

        /**
         * 警告
         */
        WARNING,

        /**
         * 严重
         */
        SEVERE,

        /**
         * 紧急
         */
        CRITICAL
    }

    public static HealthAlert create(
            LivestockId livestockId,
            AlertType type,
            AlertSeverity severity,
            String message) {

        return HealthAlert.builder()
                .id(java.util.UUID.randomUUID().toString())
                .livestockId(livestockId)
                .type(type)
                .severity(severity)
                .message(message)
                .triggeredAt(LocalDateTime.now())
                .resolved(false)
                .build();
    }
}
