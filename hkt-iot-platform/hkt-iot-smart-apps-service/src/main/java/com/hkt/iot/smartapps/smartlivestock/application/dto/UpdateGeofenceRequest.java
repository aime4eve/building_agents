package com.hkt.iot.smartapps.smartlivestock.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 更新电子围栏请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGeofenceRequest {

    /**
     * 围栏名称
     */
    private String name;

    /**
     * 边界坐标列表
     */
    private List<CoordinateDTO> boundary;

    /**
     * 描述
     */
    private String description;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinateDTO {
        private Double latitude;
        private Double longitude;
    }
}
