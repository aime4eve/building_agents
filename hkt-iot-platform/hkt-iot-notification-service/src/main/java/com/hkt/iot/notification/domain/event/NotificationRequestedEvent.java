package com.hkt.iot.notification.domain.event;

import com.hkt.iot.domain.event.AbstractDomainEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 通知请求事件
 * 当系统请求发送通知时发布此事件，用于异步处理通知发送
 *
 * @author HKT IoT Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestedEvent extends AbstractDomainEvent {

    private static final String EVENT_TYPE = "NotificationRequestedEvent";

    private Long requestId;

    private String dedupeKey;

    private String tenantId;

    private List<String> channelTypes;

    private String receiverId;

    private String receiverType;

    private String title;

    private String content;

    private String templateCode;

    private Map<String, Object> variables;

    private String businessType;

    private String businessId;

    private String correlationId;

    private Integer priority;

    private Instant requestedAt;

    private String sourceSystem;

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

    public static NotificationRequestedEvent create(Long requestId, String dedupeKey, String tenantId,
                                                    List<String> channelTypes, String receiverId, String receiverType,
                                                    String title, String content, String templateCode,
                                                    Map<String, Object> variables, String businessType,
                                                    String businessId, String correlationId, Integer priority,
                                                    String sourceSystem) {
        return NotificationRequestedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .requestId(requestId)
                .dedupeKey(dedupeKey)
                .tenantId(tenantId)
                .channelTypes(channelTypes)
                .receiverId(receiverId)
                .receiverType(receiverType)
                .title(title)
                .content(content)
                .templateCode(templateCode)
                .variables(variables)
                .businessType(businessType)
                .businessId(businessId)
                .correlationId(correlationId)
                .priority(priority)
                .requestedAt(Instant.now())
                .sourceSystem(sourceSystem)
                .build();
    }
}
