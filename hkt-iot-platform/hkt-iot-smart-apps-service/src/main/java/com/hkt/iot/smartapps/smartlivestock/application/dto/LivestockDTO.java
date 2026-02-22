package com.hkt.iot.smartapps.smartlivestock.application.dto;

import com.hkt.iot.smartapps.smartlivestock.domain.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 牲畜数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LivestockDTO {

    private String id;
    private String tag;
    private String type;
    private String typeDescription;
    private String status;
    private String statusDescription;
    private String gender;
    private String genderDescription;
    private LocalDate birthDate;
    private Integer ageInMonths;
    private Boolean isAdult;
    private Double currentWeight;
    private String weightUnit;
    private Integer healthScore;
    private String healthLevel;
    private Double latitude;
    private Double longitude;
    private String geofenceId;
    private String geofenceName;
    private String rumenCapsuleId;
    private String trackerId;
    private String tenantId;
    private String breed;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LivestockDTO fromDomain(Livestock livestock) {
        if (livestock == null) {
            return null;
        }
        
        Location location = livestock.getCurrentLocation();
        HealthScore healthScore = livestock.getHealthScore();
        Weight weight = livestock.getCurrentWeight();
        
        return LivestockDTO.builder()
                .id(livestock.getId() != null ? livestock.getId().getValue() : null)
                .tag(livestock.getTag() != null ? livestock.getTag().getValue() : null)
                .type(livestock.getType() != null ? livestock.getType().name() : null)
                .typeDescription(livestock.getType() != null ? livestock.getType().getDescription() : null)
                .status(livestock.getStatus() != null ? livestock.getStatus().name() : null)
                .statusDescription(livestock.getStatus() != null ? livestock.getStatus().getDescription() : null)
                .gender(livestock.getGender() != null ? livestock.getGender().name() : null)
                .genderDescription(livestock.getGender() != null ? livestock.getGender().getDescription() : null)
                .birthDate(livestock.getBirthDate())
                .ageInMonths(livestock.getAgeInMonths())
                .isAdult(livestock.isAdult())
                .currentWeight(weight != null ? weight.getValue() : null)
                .weightUnit(weight != null ? weight.getUnit() : null)
                .healthScore(healthScore != null ? healthScore.getValue() : null)
                .healthLevel(healthScore != null && healthScore.getLevel() != null 
                        ? healthScore.getLevel().name() : null)
                .latitude(location != null && location.getCoordinate() != null 
                        ? location.getCoordinate().getLatitude() : null)
                .longitude(location != null && location.getCoordinate() != null 
                        ? location.getCoordinate().getLongitude() : null)
                .geofenceId(livestock.getGeofenceId() != null ? livestock.getGeofenceId().getValue() : null)
                .rumenCapsuleId(livestock.getRumenCapsuleId() != null ? livestock.getRumenCapsuleId().getValue() : null)
                .trackerId(livestock.getTrackerId() != null ? livestock.getTrackerId().getValue() : null)
                .tenantId(livestock.getTenantId() != null ? livestock.getTenantId().getValue() : null)
                .breed(livestock.getBreed())
                .notes(livestock.getNotes())
                .build();
    }
}
