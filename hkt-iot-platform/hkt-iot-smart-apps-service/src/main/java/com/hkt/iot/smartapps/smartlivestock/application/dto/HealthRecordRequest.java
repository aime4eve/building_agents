package com.hkt.iot.smartapps.smartlivestock.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 健康记录请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecordRequest {
    private Double temperature;
    private Integer heartRate;
    private Integer respiratoryRate;
    private Integer steps;
    private Double feedIntake;
    private Double waterIntake;
    private Double rumenPh;
    private String recordTime;
}
