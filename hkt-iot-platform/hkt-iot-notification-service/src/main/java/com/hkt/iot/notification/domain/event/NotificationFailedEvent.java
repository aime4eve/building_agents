package com.hkt.iot.notification.domain.event;

import com.hkt.iot.domain.event.AbstractDomainEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * 通知发送失败事件
 * 当通知发送失败后发布此事件
 *
 * @author HKT IoT Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationFailedEvent extends AbstractDomainEvent {

    /**
     * 事件类型
     */
    private static final String EVENT_TYPE = "NotificationFailedEvent";

    /**
     * 请求ID
     */
    private Long requestId;

    /**
     * 幂等键
     */
    private String dedupeKey;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 通知渠道
     */
    private String channelType;

    /**
     * 接收者ID
     */
    private String receiverId;

    /**
     * 接收者类型
     */
    private String receiverType;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 失败原因
     */
    private String errorMessage;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetry;

    /**
     * 是否需要重试
     */
    private Boolean needRetry;

    /**
     * 下次重试时间
     */
    private Instant nextRetryAt;

    /**
     * 关联业务类型
     */
    private String businessType;

    /**
     * 关联业务ID
     */
    private String businessId;

    /**
     * CorrelationID
     */
    private String correlationId;

    /**
     * 失败时间
     */
    private Instant failedAt;

    @Override
    public String getAggregateId() {
        return requestId != null ? requestId.toString() : null;
    }

    @Override
    public String getAggregateType() {
        return "NotificationRequest";
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    /**
     * 创建事件
     */
    public static NotificationFailedEvent create(Long requestId, String dedupeKey, String tenantId,
                                                  String channelType, String receiverId, String receiverType,
                                                  String title, String errorMessage, String errorCode,
                                                  Integer retryCount, Integer maxRetry, Boolean needRetry,
                                                  Instant nextRetryAt, String businessType, String businessId,
                                                  String correlationId) {
        return NotificationFailedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .requestId(requestId)
                .dedupeKey(dedupeKey)
                .tenantId(tenantId)
                .channelType(channelType)
                .receiverId(receiverId)
                .receiverType(receiverType)
                .title(title)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .retryCount(retryCount)
                .maxRetry(maxRetry)
                .needRetry(needRetry)
                .nextRetryAt(nextRetryAt)
                .businessType(businessType)
                .businessId(businessId)
                .correlationId(correlationId)
                .failedAt(Instant.now())
                .build();
    }
}
