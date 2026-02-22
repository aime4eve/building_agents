package com.hkt.iot.smartapps.smartlivestock.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新牲畜请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLivestockRequest {
    private String breed;
    private String notes;
}
