package com.hkt.iot.smartapps.smartlivestock.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 位置更新请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequest {
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private String timestamp;
}
