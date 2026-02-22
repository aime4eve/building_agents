package com.hkt.iot.scene.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 更新定时计划请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScheduleRequest {

    private String name;

    private String description;

    private String cronExpression;

    private LocalDateTime validFrom;

    private LocalDateTime validTo;
}
