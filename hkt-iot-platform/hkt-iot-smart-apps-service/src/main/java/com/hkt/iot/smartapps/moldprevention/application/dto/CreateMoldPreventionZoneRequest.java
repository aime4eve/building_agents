package com.hkt.iot.smartapps.moldprevention.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建防霉管控区域请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMoldPreventionZoneRequest {
    private String name;
    private String spaceId;
    private String tenantId;
    private String description;
    private Double humidityThresholdLow;
    private Double humidityThresholdHigh;
}
