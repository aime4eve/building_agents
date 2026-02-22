package com.hkt.iot.scene.application.dto;

import com.hkt.iot.domain.shared.DeviceId;
import com.hkt.iot.scene.domain.model.SceneTrigger;
import com.hkt.iot.scene.domain.model.TriggerId;
import com.hkt.iot.scene.domain.model.TriggerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 场景触发条件DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneTriggerDTO {

    private String id;
    private String type;
    private String condition;
    private String deviceId;
    private String eventIdentifier;
    private int priority;
    private Map<String, Object> parameters;

    public static SceneTriggerDTO from(SceneTrigger trigger) {
        return SceneTriggerDTO.builder()
                .id(trigger.getId().getValue())
                .type(trigger.getType().name())
                .condition(trigger.getCondition())
                .deviceId(trigger.getDeviceId() != null ? trigger.getDeviceId().getValue() : null)
                .eventIdentifier(trigger.getEventIdentifier())
                .priority(trigger.getPriority())
                .parameters(trigger.getParameters())
                .build();

    }

    public SceneTrigger toDomain() {
        return SceneTrigger.builder()
                .id(TriggerId.of(this.id))
                .type(TriggerType.valueOf(this.type))
                .condition(this.condition)
                .deviceId(this.deviceId != null ? DeviceId.of(this.deviceId) : null)
                .eventIdentifier(this.eventIdentifier)
                .priority(this.priority)
                .parameters(this.parameters)
                .build();
    }
}
