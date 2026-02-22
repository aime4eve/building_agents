package com.hkt.iot.notification.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 通知发送请求
 *
 * @author HKT IoT Team
 */
@Schema(description = "通知发送请求")
public class NotificationSendRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "幂等键，用于去重")
    private String dedupeKey;

    @Schema(description = "租户ID", required = true)
    @NotBlank(message = "租户ID不能为空")
    private String tenantId;

    @Schema(description = "通知渠道: PUSH/EMAIL/SMS/IN_APP/WEBHOOK", required = true)
    @NotBlank(message = "通知渠道不能为空")
    private String channelType;

    @Schema(description = "接收者类型: USER/ROLE/GROUP/DEVICE", required = true)
    @NotBlank(message = "接收者类型不能为空")
    private String receiverType;

    @Schema(description = "接收者ID", required = true)
    @NotBlank(message = "接收者ID不能为空")
    private String receiverId;

    @Schema(description = "接收者地址（邮箱/手机号）")
    private String receiverAddress;

    @Schema(description = "模板编码", required = true)
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    @Schema(description = "模板变量")
    private Map<String, Object> variables;

    @Schema(description = "优先级: LOW/NORMAL/HIGH/URGENT")
    private String priority = "NORMAL";

    @Schema(description = "预定发送时间（Unix时间戳）")
    private Long scheduledAt;

    @Schema(description = "关联业务类型")
    private String businessType;

    @Schema(description = "关联业务ID")
    private String businessId;

    @Schema(description = "CorrelationID，用于链路追踪")
    private String correlationId;

    @Schema(description = "Webhook URL")
    private String webhookUrl;

    @Schema(description = "Webhook请求方法: GET/POST")
    private String webhookMethod = "POST";

    @Schema(description = "Webhook请求头")
    private Map<String, String> webhookHeaders;

    public NotificationSendRequest() {
    }

    public String getDedupeKey() {
        return dedupeKey;
    }

    public void setDedupeKey(String dedupeKey) {
        this.dedupeKey = dedupeKey;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Long scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getWebhookMethod() {
        return webhookMethod;
    }

    public void setWebhookMethod(String webhookMethod) {
        this.webhookMethod = webhookMethod;
    }

    public Map<String, String> getWebhookHeaders() {
        return webhookHeaders;
    }

    public void setWebhookHeaders(Map<String, String> webhookHeaders) {
        this.webhookHeaders = webhookHeaders;
    }
}
