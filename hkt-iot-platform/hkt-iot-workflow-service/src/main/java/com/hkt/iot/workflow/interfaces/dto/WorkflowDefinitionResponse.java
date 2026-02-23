package com.hkt.iot.workflow.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 流程定义响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "流程定义响应")
public class WorkflowDefinitionResponse {

    @Schema(description = "流程定义ID")
    private String definitionId;

    @Schema(description = "流程定义键")
    private String definitionKey;

    @Schema(description = "流程名称")
    private String name;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;
}
