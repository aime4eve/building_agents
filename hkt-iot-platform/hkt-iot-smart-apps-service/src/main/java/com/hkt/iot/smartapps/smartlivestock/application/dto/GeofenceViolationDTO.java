package com.hkt.iot.smartapps.smartlivestock.application.dto;

import com.hkt.iot.smartapps.smartlivestock.domain.model.GeofenceViolation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 电子围栏违规DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceViolationDTO {

    /**
     * 违规ID
     */
    private String id;

    /**
     * 牲畜ID
     */
    private String livestockId;

    /**
     * 牲畜耳标
     */
    private String livestockTag;

    /**
     * 围栏ID
     */
    private String geofenceId;

    /**
     * 围栏名称
     */
    private String geofenceName;

    /**
     * 违规类型
     */
    private String violationType;

    /**
     * 违规时纬度
     */
    private Double latitude;

    /**
     * 违规时经度
     */
    private Double longitude;

    /**
     * 违规时间
     */
    private LocalDateTime violationTime;

    /**
     * 是否已解决
     */
    private Boolean resolved;

    /**
     * 解决时间
     */
    private LocalDateTime resolvedAt;

    /**
     * 解决说明
     */
    private String resolveNote;

    public static GeofenceViolationDTO fromDomain(GeofenceViolation violation) {
        if (violation == null) {
            return null;
        }
        
        return GeofenceViolationDTO.builder()
                .id(violation.getId() != null ? violation.getId().getValue() : null)
                .livestockId(violation.getLivestockId() != null ? violation.getLivestockId().getValue() : null)
                .geofenceId(violation.getGeofenceId() != null ? violation.getGeofenceId().getValue() : null)
                .violationType(violation.getViolationType() != null ? violation.getViolationType().name() : null)
                .latitude(violation.getLocation() != null && violation.getLocation().getCoordinate() != null 
                        ? violation.getLocation().getCoordinate().getLatitude() : null)
                .longitude(violation.getLocation() != null && violation.getLocation().getCoordinate() != null 
                        ? violation.getLocation().getCoordinate().getLongitude() : null)
                .violationTime(violation.getViolationTime())
                .resolved(violation.isResolved())
                .resolvedAt(violation.getResolvedAt())
                .resolveNote(violation.getResolveNote())
                .build();
    }
}
