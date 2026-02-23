package com.hkt.iot.workflow.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单 DTO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@Schema(description = "工单信息")
public class WorkOrderDTO {

    @Schema(description = "工单ID")
    String id;

    @Schema(description = "工单编号")
    String workOrderNo;

    @Schema(description = "工单标题")
    String title;

    @Schema(description = "工单描述")
    String description;

    @Schema(description = "工单类型")
    String type;

    @Schema(description = "工单类型描述")
    String typeDesc;

    @Schema(description = "工单状态")
    String status;

    @Schema(description = "工单状态描述")
    String statusDesc;

    @Schema(description = "工单优先级")
    String priority;

    @Schema(description = "工单优先级描述")
    String priorityDesc;

    @Schema(description = "流程实例ID")
    String processInstanceId;

    @Schema(description = "模板ID")
    String templateId;

    @Schema(description = "空间ID")
    String spaceId;

    @Schema(description = "报告人ID")
    String reporterId;

    @Schema(description = "指派人ID")
    String assigneeId;

    @Schema(description = "处理人ID")
    String handlerId;

    @Schema(description = "截止时间")
    LocalDateTime dueTime;

    @Schema(description = "完成时间")
    LocalDateTime completedAt;

    @Schema(description = "租户ID")
    String tenantId;

    @Schema(description = "创建时间")
    LocalDateTime createdAt;

    @Schema(description = "更新时间")
    LocalDateTime updatedAt;

    @Schema(description = "是否超时")
    Boolean overdue;
}
