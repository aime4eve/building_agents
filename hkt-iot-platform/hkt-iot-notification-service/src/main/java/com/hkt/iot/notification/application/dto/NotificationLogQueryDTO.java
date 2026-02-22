package com.hkt.iot.notification.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 通知日志查询DTO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知日志查询条件")
public class NotificationLogQueryDTO {

    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    private String tenantId;

    /**
     * 通知渠道
     */
    @Schema(description = "通知渠道")
    private String channelType;

    /**
     * 接收者ID
     */
    @Schema(description = "接收者ID")
    private String receiverId;

    /**
     * 发送状态
     */
    @Schema(description = "发送状态")
    private String status;

    /**
     * CorrelationID
     */
    @Schema(description = "CorrelationID")
    private String correlationId;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间（Unix时间戳）")
    private Long startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间（Unix时间戳）")
    private Long endTime;

    /**
     * 页码
     */
    @Schema(description = "页码")
    @Builder.Default
    private Integer page = 1;

    /**
     * 每页数量
     */
    @Schema(description = "每页数量")
    @Builder.Default
    private Integer size = 20;
}
