package com.hkt.iot.smartapps.smartlivestock.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建电子围栏请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGeofenceRequest {

    /**
     * 围栏名称
     */
    private String name;

    /**
     * 围栏类型
     */
    private String type;

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
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinateDTO {
        private Double latitude;
        private Double longitude;
    }
}
