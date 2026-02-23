package com.hkt.iot.workflow.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建流程定义请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建流程定义请求")
public class CreateWorkflowDefinitionRequest {

    @NotBlank(message = "租户ID不能为空")
    @Schema(description = "租户ID", required = true)
    private String tenantId;

    @NotBlank(message = "流程定义键不能为空")
    @Schema(description = "流程定义键", required = true)
    private String definitionKey;

    @NotBlank(message = "流程名称不能为空")
    @Schema(description = "流程名称", required = true)
    private String name;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "描述")
    private String description;

    @NotBlank(message = "创建人不能为空")
    @Schema(description = "创建人ID", required = true)
    private String createdBy;
}
