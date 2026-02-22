package com.hkt.iot.scene.application.dto;

import com.hkt.iot.domain.shared.SpaceId;
import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.scene.domain.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 场景DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneDTO {

    private String id;
    private String name;
    private String code;
    private String type;
    private String status;
    private List<SceneTriggerDTO> triggers;
    private List<SceneActionDTO> actions;
    private String executionMode;
    private String tenantId;
    private String spaceId;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    public static SceneDTO from(Scene scene) {
        return SceneDTO.builder()
                .id(scene.getId().getValue())
                .name(scene.getName().getValue())
                .code(scene.getCode().getValue())
                .type(scene.getType().name())
                .status(scene.getStatus().name())
                .triggers(scene.getTriggers() != null
                    ? scene.getTriggers().stream()
                        .map(SceneTriggerDTO::from)
                        .toList()
                    : List.of())
                .actions(scene.getActions() != null
                    ? scene.getActions().stream()
                        .map(SceneActionDTO::from)
                        .toList()
                    : List.of())
                .executionMode(scene.getExecutionMode().name())
                .tenantId(scene.getTenantId() != null ? scene.getTenantId().getValue() : null)
                .spaceId(scene.getSpaceId() != null ? scene.getSpaceId().getValue() : null)
                .description(scene.getDescription())
                .createdAt(scene.getAuditLog() != null ? scene.getAuditLog().getCreatedAt() : null)
                .updatedAt(scene.getAuditLog() != null ? scene.getAuditLog().getUpdatedAt() : null)
                .version(scene.getVersion())
                .build();
    }
}
