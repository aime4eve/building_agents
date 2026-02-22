package com.hkt.iot.scene.application.dto;

import com.hkt.iot.domain.shared.DeviceId;
import com.hkt.iot.scene.domain.model.ActionId;
import com.hkt.iot.scene.domain.model.ActionType;
import com.hkt.iot.scene.domain.model.SceneAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 场景执行动作DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneActionDTO {

    private String id;
    private String type;
    private String deviceId;
    private String serviceIdentifier;
    private Map<String, Object> params;
    private int delaySeconds;
    private int order;

    public static SceneActionDTO from(SceneAction action) {
        return SceneActionDTO.builder()
                .id(action.getId().getValue())
                .type(action.getType().name())
                .deviceId(action.getDeviceId() != null ? action.getDeviceId().getValue() : null)
                .serviceIdentifier(action.getServiceIdentifier())
                .params(action.getParams())
                .delaySeconds(action.getDelaySeconds())
                .order(action.getOrder())
                .build();
    }

    public SceneAction toDomain() {
        return SceneAction.builder()
                .id(ActionId.of(this.id))
                .type(ActionType.valueOf(this.type))
                .deviceId(this.deviceId != null ? DeviceId.of(this.deviceId) : null)
                .serviceIdentifier(this.serviceIdentifier)
                .params(this.params)
                .delaySeconds(this.delaySeconds)
                .order(this.order)
                .build();
    }
}
