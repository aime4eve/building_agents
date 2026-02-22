package com.hkt.iot.notification.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 通知发送DTO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知发送请求")
public class NotificationSendDTO {

    /**
     * 幂等键（可选，用于去重）
     */
    @Schema(description = "幂等键，用于去重")
    private String dedupeKey;

    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    @NotBlank(message = "租户ID不能为空")
    private String tenantId;

    /**
     * 通知渠道
     */
    @Schema(description = "通知渠道: PUSH/EMAIL/SMS/IN_APP/WEBHOOK")
    @NotBlank(message = "通知渠道不能为空")
    private String channelType;

    /**
     * 接收者类型
     */
    @Schema(description = "接收者类型: USER/ROLE/GROUP/DEVICE")
    @NotBlank(message = "接收者类型不能为空")
    private String receiverType;

    /**
     * 接收者ID
     */
    @Schema(description = "接收者ID")
    @NotBlank(message = "接收者ID不能为空")
    private String receiverId;

    /**
     * 接收者地址（邮箱/手机号，可选）
     */
    @Schema(description = "接收者地址（邮箱/手机号）")
    private String receiverAddress;

    /**
     * 模板编码
     */
    @Schema(description = "模板编码")
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    /**
     * 模板变量
     */
    @Schema(description = "模板变量")
    private Map<String, Object> variables;

    /**
     * 优先级
     */
    @Schema(description = "优先级: LOW/NORMAL/HIGH/URGENT")
    @Builder.Default
    private String priority = "NORMAL";

    /**
     * 预定发送时间（可选，为空则立即发送）
     */
    @Schema(description = "预定发送时间（Unix时间戳）")
    private Long scheduledAt;

    /**
     * 关联业务类型
     */
    @Schema(description = "关联业务类型")
    private String businessType;

    /**
     * 关联业务ID
     */
    @Schema(description = "关联业务ID")
    private String businessId;

    /**
     * CorrelationID（用于链路追踪）
     */
    @Schema(description = "CorrelationID，用于链路追踪")
    private String correlationId;

    /**
     * Webhook URL（仅渠道为WEBHOOK时有效）
     */
    @Schema(description = "Webhook URL")
    private String webhookUrl;

    /**
     * Webhook方法（仅渠道为WEBHOOK时有效）
     */
    @Schema(description = "Webhook请求方法: GET/POST")
    @Builder.Default
    private String webhookMethod = "POST";

    /**
     * Webhook请求头（仅渠道为WEBHOOK时有效）
     */
    @Schema(description = "Webhook请求头")
    private Map<String, String> webhookHeaders;
}
