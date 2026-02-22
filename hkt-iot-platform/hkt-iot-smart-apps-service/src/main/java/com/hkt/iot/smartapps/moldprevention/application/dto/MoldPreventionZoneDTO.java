package com.hkt.iot.smartapps.moldprevention.application.dto;

import com.hkt.iot.smartapps.moldprevention.domain.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 防霉管控区域DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoldPreventionZoneDTO {

    private String id;
    private String name;
    private String code;
    private String spaceId;
    private String tenantId;
    private String status;
    private String statusDescription;
    private String currentRiskLevel;
    private String riskLevelDescription;
    private Double currentTemperature;
    private Double currentHumidity;
    private LocalDateTime lastEvaluatedAt;
    private String description;
    private List<SensorDTO> sensors;
    private List<ControllerDTO> controllers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensorDTO {
        private String id;
        private String name;
        private String type;
        private Boolean online;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ControllerDTO {
        private String id;
        private String name;
        private String type;
        private Boolean online;
    }

    public static MoldPreventionZoneDTO fromDomain(MoldPreventionZone zone) {
        if (zone == null) {
            return null;
        }

        List<SensorDTO> sensorDTOs = null;
        if (zone.getSensors() != null) {
            sensorDTOs = zone.getSensors().stream()
                    .map(s -> SensorDTO.builder()
                            .id(s.getId().getValue())
                            .type(s.getType())
                            .online(s.isOnline())
                            .build())
                    .collect(Collectors.toList());
        }

        List<ControllerDTO> controllerDTOs = null;
        if (zone.getControllers() != null) {
            controllerDTOs = zone.getControllers().stream()
                    .map(c -> ControllerDTO.builder()
                            .id(c.getId().getValue())
                            .type(c.getType().name())
                            .online(c.isOnline())
                            .build())
                    .collect(Collectors.toList());
        }

        EnvironmentData envData = zone.getLastEnvironmentData();

        return MoldPreventionZoneDTO.builder()
                .id(zone.getId().getValue())
                .name(zone.getName().getValue())
                .code(zone.getCode().getValue())
                .spaceId(zone.getSpaceId() != null ? zone.getSpaceId().getValue() : null)
                .tenantId(zone.getTenantId() != null ? zone.getTenantId().getValue() : null)
                .status(zone.getStatus().name())
                .statusDescription(zone.getStatus().getDescription())
                .currentRiskLevel(zone.getCurrentRiskLevel() != null ? zone.getCurrentRiskLevel().name() : null)
                .riskLevelDescription(zone.getCurrentRiskLevel() != null ? zone.getCurrentRiskLevel().getDescription() : null)
                .currentTemperature(envData != null ? envData.getTemperature() : null)
                .currentHumidity(envData != null ? envData.getHumidity() : null)
                .lastEvaluatedAt(zone.getLastEvaluatedAt())
                .description(zone.getDescription())
                .sensors(sensorDTOs)
                .controllers(controllerDTOs)
                .build();
    }
}
