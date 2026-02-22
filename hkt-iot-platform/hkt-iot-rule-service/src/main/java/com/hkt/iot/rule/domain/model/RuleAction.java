package com.hkt.iot.rule.domain.model;

import java.util.Map;

/**
 * 规则动作实体
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class RuleAction {
    private Long id;
    private String code;
    private String name;
    private ActionType type;
    private Integer order;
    private Integer delaySeconds;
    private Map<String, Object> config;

    // 设备控制专用
    private Long targetDeviceId;
    private String serviceIdentifier;
    private Map<String, Object> controlParams;

    // 通知专用
    private String notificationChannel;
    private String notificationTemplate;
    private java.util.List<Long> notificationReceivers;

    // Webhook专用
    private String webhookUrl;
    private String webhookMethod;
    private Map<String, String> webhookHeaders;

    // 执行配置
    private Boolean async;
    private Integer timeoutSeconds;

    /**
     * 动作类型
     */
    public enum ActionType {
        DEVICE_CONTROL,   // 设备控制
        NOTIFICATION,      // 通知
        WEBHOOK,          // Webhook
        WORKFLOW          // 工作流
    }

    /**
     * 私有构造函数
     */
    private RuleAction() {
    }

    /**
     * 创建动作
     */
    public static RuleAction create(String code, String name, ActionType type, Integer order) {
        RuleAction action = new RuleAction();
        action.code = code;
        action.name = name;
        action.type = type;
        action.order = order;
        action.delaySeconds = 0;
        action.async = true;
        action.timeoutSeconds = 30;
        return action;
    }

    /**
     * 执行动作
     */
    public ActionResult execute(RuleContext context) {
        // 这里应该调用相应的执行器
        // Phase 1 简化实现：只返回成功结果
        return ActionResult.success("Action executed: " + name);
    }

    // ==================== Getters and Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Integer getDelaySeconds() {
        return delaySeconds;
    }

    public void setDelaySeconds(Integer delaySeconds) {
        this.delaySeconds = delaySeconds;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public Long getTargetDeviceId() {
        return targetDeviceId;
    }

    public void setTargetDeviceId(Long targetDeviceId) {
        this.targetDeviceId = targetDeviceId;
    }

    public String getServiceIdentifier() {
        return serviceIdentifier;
    }

    public void setServiceIdentifier(String serviceIdentifier) {
        this.serviceIdentifier = serviceIdentifier;
    }

    public Map<String, Object> getControlParams() {
        return controlParams;
    }

    public void setControlParams(Map<String, Object> controlParams) {
        this.controlParams = controlParams;
    }

    public String getNotificationChannel() {
        return notificationChannel;
    }

    public void setNotificationChannel(String notificationChannel) {
        this.notificationChannel = notificationChannel;
    }

    public String getNotificationTemplate() {
        return notificationTemplate;
    }

    public void setNotificationTemplate(String notificationTemplate) {
        this.notificationTemplate = notificationTemplate;
    }

    public java.util.List<Long> getNotificationReceivers() {
        return notificationReceivers;
    }

    public void setNotificationReceivers(java.util.List<Long> notificationReceivers) {
        this.notificationReceivers = notificationReceivers;
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

    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
