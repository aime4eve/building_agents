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
 * 通知发送成功事件
 * 当通知成功发送后发布此事件
 *
 * @author HKT IoT Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSentEvent extends AbstractDomainEvent {

    /**
     * 事件类型
     */
    private static final String EVENT_TYPE = "NotificationSentEvent";

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
     * 模板编码
     */
    private String templateCode;

    /**
     * 模板变量
     */
    private Map<String, Object> variables;

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
     * 发送时间
     */
    private Instant sentAt;

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
    public static NotificationSentEvent create(Long requestId, String dedupeKey, String tenantId,
                                               String channelType, String receiverId, String receiverType,
                                               String title, String templateCode, Map<String, Object> variables,
                                               String businessType, String businessId, String correlationId) {
        return NotificationSentEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .requestId(requestId)
                .dedupeKey(dedupeKey)
                .tenantId(tenantId)
                .channelType(channelType)
                .receiverId(receiverId)
                .receiverType(receiverType)
                .title(title)
                .templateCode(templateCode)
                .variables(variables)
                .businessType(businessType)
                .businessId(businessId)
                .correlationId(correlationId)
                .sentAt(Instant.now())
                .build();
    }
}
