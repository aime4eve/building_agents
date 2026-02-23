package com.hkt.iot.workflow.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单模板 DTO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@Schema(description = "工单模板信息")
public class WorkOrderTemplateDTO {

    @Schema(description = "模板ID")
    String id;

    @Schema(description = "模板名称")
    String name;

    @Schema(description = "工单类型")
    String type;

    @Schema(description = "工单类型描述")
    String typeDesc;

    @Schema(description = "流程定义Key")
    String processDefinitionKey;

    @Schema(description = "自定义字段配置(JSON)")
    String customFields;

    @Schema(description = "租户ID")
    String tenantId;

    @Schema(description = "创建时间")
    LocalDateTime createdAt;

    @Schema(description = "更新时间")
    LocalDateTime updatedAt;
}
