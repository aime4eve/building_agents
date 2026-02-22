package com.hkt.iot.smartapps.smartlivestock.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 牲畜查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LivestockQueryRequest {
    private String type;
    private String status;
    private String healthLevel;
    private String geofenceId;
    private String keyword;
    private Integer pageNum;
    private Integer pageSize;
}
