package com.hkt.iot.smartapps.smartlivestock.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 创建牲畜请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLivestockRequest {

    /**
     * 耳标号
     */
    private String tag;

    /**
     * 牲畜类型
     */
    private String type;

    /**
     * 性别
     */
    private String gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 体重
     */
    private Double weight;

    /**
     * 品种
     */
    private String breed;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 备注
     */
    private String notes;
}
