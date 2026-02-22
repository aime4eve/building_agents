package com.hkt.iot.scene.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建定时计划请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleRequest {

    @NotBlank(message = "定时计划名称不能为空")
    private String name;

    @NotBlank(message = "定时计划编码不能为空")
    private String code;

    @NotNull(message = "定时计划类型不能为空")
    private String type;

    private String cronExpression;

    private String description;

    private String spaceId;

    private LocalDateTime validFrom;

    private LocalDateTime validTo;

    private List<SceneActionDTO> actions;
}
