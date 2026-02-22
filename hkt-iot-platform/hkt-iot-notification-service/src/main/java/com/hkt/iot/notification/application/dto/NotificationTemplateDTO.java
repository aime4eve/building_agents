package com.hkt.iot.notification.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知模板DTO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知模板")
public class NotificationTemplateDTO {

    /**
     * 模板ID
     */
    @Schema(description = "模板ID")
    private Long id;

    /**
     * 模板编码
     */
    @Schema(description = "模板编码")
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称")
    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    /**
     * 模板类型
     */
    @Schema(description = "模板类型: ALARM/SYSTEM/BUSINESS")
    @NotBlank(message = "模板类型不能为空")
    private String templateType;

    /**
     * 通知渠道
     */
    @Schema(description = "通知渠道: PUSH/EMAIL/SMS/IN_APP/WEBHOOK")
    @NotBlank(message = "通知渠道不能为空")
    private String channelType;

    /**
     * 模板标题
     */
    @Schema(description = "模板标题")
    private String titleTemplate;

    /**
     * 模板内容
     */
    @Schema(description = "模板内容")
    @NotBlank(message = "模板内容不能为空")
    private String contentTemplate;

    /**
     * 模板变量（JSON格式）
     */
    @Schema(description = "模板变量定义（JSON格式）")
    private String variables;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    @Builder.Default
    private Boolean enabled = true;
}
