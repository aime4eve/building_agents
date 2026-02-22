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
 * 告警触发事件
 * 当设备告警触发时发布此事件，用于触发通知流程
 *
 * @author HKT IoT Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmTriggeredEvent extends AbstractDomainEvent {

    private static final String EVENT_TYPE = "AlarmTriggeredEvent";

    private String alarmId;

    private String tenantId;

    private Long deviceId;

    private String deviceName;

    private String alarmType;

    private String alarmLevel;

    private String alarmTitle;

    private String alarmMessage;

    private Map<String, Object> alarmData;

    private Instant triggeredAt;

    private String ruleId;

    private String ruleName;

    @Override
    public String getAggregateId() {
        return alarmId;
    }

    @Override
    public String getAggregateType() {
        return "Alarm";
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    public static AlarmTriggeredEvent create(String alarmId, String tenantId, Long deviceId,
                                             String deviceName, String alarmType, String alarmLevel,
                                             String alarmTitle, String alarmMessage,
                                             Map<String, Object> alarmData, String ruleId, String ruleName) {
        return AlarmTriggeredEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .alarmId(alarmId)
                .tenantId(tenantId)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .alarmType(alarmType)
                .alarmLevel(alarmLevel)
                .alarmTitle(alarmTitle)
                .alarmMessage(alarmMessage)
                .alarmData(alarmData)
                .triggeredAt(Instant.now())
                .ruleId(ruleId)
                .ruleName(ruleName)
                .build();
    }
}
