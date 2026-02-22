package com.hkt.iot.scene.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建场景请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSceneRequest {

    @NotBlank(message = "场景名称不能为空")
    private String name;

    @NotBlank(message = "场景编码不能为空")
    private String code;

    @NotNull(message = "场景类型不能为空")
    private String type;

    private String description;

    private String spaceId;

    private List<SceneTriggerDTO> triggers;

    private List<SceneActionDTO> actions;

    private String executionMode;
}
