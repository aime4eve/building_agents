package com.hkt.iot.smartapps.smartlivestock.application.dto;

import com.hkt.iot.smartapps.smartlivestock.domain.model.Coordinate;
import com.hkt.iot.smartapps.smartlivestock.domain.model.Geofence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 电子围栏DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceDTO {

    /**
     * 围栏ID
     */
    private String id;

    /**
     * 围栏名称
     */
    private String name;

    /**
     * 围栏编码
     */
    private String code;

    /**
     * 围栏类型
     */
    private String type;

    /**
     * 围栏类型描述
     */
    private String typeDescription;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusDescription;

    /**
     * 边界坐标列表
     */
    private List<CoordinateDTO> boundary;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 描述
     */
    private String description;

    /**
     * 关联牲畜数量
     */
    private Integer livestockCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinateDTO {
        private Double latitude;
        private Double longitude;
        
        public static CoordinateDTO fromDomain(Coordinate coordinate) {
            if (coordinate == null) {
                return null;
            }
            return CoordinateDTO.builder()
                    .latitude(coordinate.getLatitude())
                    .longitude(coordinate.getLongitude())
                    .build();
        }
    }

    public static GeofenceDTO fromDomain(Geofence geofence) {
        if (geofence == null) {
            return null;
        }
        
        List<CoordinateDTO> boundaryDTO = null;
        if (geofence.getBoundary() != null) {
            boundaryDTO = geofence.getBoundary().stream()
                    .map(CoordinateDTO::fromDomain)
                    .collect(Collectors.toList());
        }
        
        return GeofenceDTO.builder()
                .id(geofence.getId() != null ? geofence.getId().getValue() : null)
                .name(geofence.getName() != null ? geofence.getName().getValue() : null)
                .code(geofence.getCode() != null ? geofence.getCode().getValue() : null)
                .type(geofence.getType() != null ? geofence.getType().name() : null)
                .typeDescription(geofence.getType() != null ? geofence.getType().getDescription() : null)
                .status(geofence.getStatus() != null ? geofence.getStatus().name() : null)
                .statusDescription(geofence.getStatus() != null ? geofence.getStatus().getDescription() : null)
                .boundary(boundaryDTO)
                .tenantId(geofence.getTenantId() != null ? geofence.getTenantId().getValue() : null)
                .description(geofence.getDescription())
                .build();
    }
}
