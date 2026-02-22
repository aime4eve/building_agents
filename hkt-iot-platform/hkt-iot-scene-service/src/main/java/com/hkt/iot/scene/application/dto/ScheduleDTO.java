package com.hkt.iot.scene.application.dto;

import com.hkt.iot.scene.domain.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时计划DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {

    private String id;
    private String name;
    private String code;
    private String cronExpression;
    private String type;
    private List<SceneActionDTO> actions;
    private String status;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String tenantId;
    private String spaceId;
    private String description;
    private LocalDateTime lastExecutedAt;
    private LocalDateTime nextExecuteAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    public static ScheduleDTO from(Schedule schedule) {
        return ScheduleDTO.builder()
                .id(schedule.getId().getValue())
                .name(schedule.getName().getValue())
                .code(schedule.getCode().getValue())
                .cronExpression(schedule.getCronExpression() != null ? schedule.getCronExpression().getValue() : null)
                .type(schedule.getType().name())
                .actions(schedule.getActions() != null
                    ? schedule.getActions().stream()
                        .map(SceneActionDTO::from)
                        .toList()
                    : List.of())
                .status(schedule.getStatus().name())
                .validFrom(schedule.getValidFrom())
                .validTo(schedule.getValidTo())
                .tenantId(schedule.getTenantId() != null ? schedule.getTenantId().getValue() : null)
                .spaceId(schedule.getSpaceId() != null ? schedule.getSpaceId().getValue() : null)
                .description(schedule.getDescription())
                .lastExecutedAt(schedule.getLastExecutedAt())
                .nextExecuteAt(schedule.getNextExecuteAt())
                .createdAt(schedule.getAuditLog() != null ? schedule.getAuditLog().getCreatedAt() : null)
                .updatedAt(schedule.getAuditLog() != null ? schedule.getAuditLog().getUpdatedAt() : null)
                .version(schedule.getVersion())
                .build();
    }
}
