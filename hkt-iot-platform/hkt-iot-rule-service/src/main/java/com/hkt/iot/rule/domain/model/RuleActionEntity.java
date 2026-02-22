package com.hkt.iot.rule.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 规则动作实体 (JPA)
 * 基于DDL: rule_action表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "rule_action")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RuleActionEntity extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "action_code", nullable = false, length = 100)
    private String actionCode;

    @Column(name = "action_name", length = 200)
    private String actionName;

    @Column(name = "action_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column(name = "action_order", nullable = false)
    private Integer actionOrder;

    @Column(name = "delay_seconds")
    private Integer delaySeconds;

    @Column(name = "action_config", columnDefinition = "JSON", nullable = false)
    @Transient
    private Map<String, Object> actionConfig;

    // 设备控制专用
    @Column(name = "target_device_id")
    private Long targetDeviceId;

    @Column(name = "service_identifier", length = 100)
    private String serviceIdentifier;

    @Column(name = "control_params", columnDefinition = "JSON")
    @Transient
    private Map<String, Object> controlParams;

    // 通知专用
    @Column(name = "notification_channel", length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationChannel notificationChannel;

    @Column(name = "notification_template", length = 100)
    private String notificationTemplate;

    @Column(name = "notification_receivers", columnDefinition = "JSON")
    @Transient
    private List<Long> notificationReceivers;

    // Webhook专用
    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @Column(name = "webhook_method", length = 10)
    private String webhookMethod;

    @Column(name = "webhook_headers", columnDefinition = "JSON")
    @Transient
    private Map<String, String> webhookHeaders;

    @Column(name = "retry_config", columnDefinition = "JSON")
    @Transient
    private Map<String, Object> retryConfig;

    // 工作流专用
    @Column(name = "workflow_code", length = 100)
    private String workflowCode;

    @Column(name = "workflow_params", columnDefinition = "JSON")
    @Transient
    private Map<String, Object> workflowParams;

    // 执行配置
    @Column(name = "is_async")
    private Boolean isAsync;

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 动作类型
     */
    public enum ActionType {
        DEVICE_CONTROL, NOTIFICATION, WEBHOOK, WORKFLOW
    }

    /**
     * 通知渠道
     */
    public enum NotificationChannel {
        SMS, EMAIL, WEB, APP
    }

    /**
     * 工厂方法：创建规则动作
     */
    public static RuleActionEntity create(
            Long tenantId,
            Long ruleId,
            String ruleCode,
            String actionCode,
            ActionType actionType,
            Integer actionOrder,
            Map<String, Object> actionConfig) {
        RuleActionEntity action = new RuleActionEntity();
        action.tenantId = tenantId;
        action.ruleId = ruleId;
        action.ruleCode = ruleCode;
        action.actionCode = actionCode;
        action.actionType = actionType;
        action.actionOrder = actionOrder;
        action.actionConfig = actionConfig;
        action.delaySeconds = 0;
        action.isAsync = true;
        action.timeoutSeconds = 30;
        action.webhookMethod = "POST";
        action.createdAt = LocalDateTime.now();
        action.updatedAt = LocalDateTime.now();
        return action;
    }

    /**
     * 创建设备控制动作
     */
    public static RuleActionEntity createDeviceControlAction(
            Long tenantId,
            Long ruleId,
            String ruleCode,
            String actionCode,
            Integer actionOrder,
            Long targetDeviceId,
            String serviceIdentifier,
            Map<String, Object> controlParams) {
        RuleActionEntity action = new RuleActionEntity();
        action.tenantId = tenantId;
        action.ruleId = ruleId;
        action.ruleCode = ruleCode;
        action.actionCode = actionCode;
        action.actionType = ActionType.DEVICE_CONTROL;
        action.actionOrder = actionOrder;
        action.targetDeviceId = targetDeviceId;
        action.serviceIdentifier = serviceIdentifier;
        action.controlParams = controlParams;
        action.delaySeconds = 0;
        action.isAsync = false;
        action.timeoutSeconds = 30;
        action.createdAt = LocalDateTime.now();
        action.updatedAt = LocalDateTime.now();
        return action;
    }

    /**
     * 创建通知动作
     */
    public static RuleActionEntity createNotificationAction(
            Long tenantId,
            Long ruleId,
            String ruleCode,
            String actionCode,
            Integer actionOrder,
            NotificationChannel notificationChannel,
            String notificationTemplate,
            List<Long> notificationReceivers) {
        RuleActionEntity action = new RuleActionEntity();
        action.tenantId = tenantId;
        action.ruleId = ruleId;
        action.ruleCode = ruleCode;
        action.actionCode = actionCode;
        action.actionType = ActionType.NOTIFICATION;
        action.actionOrder = actionOrder;
        action.notificationChannel = notificationChannel;
        action.notificationTemplate = notificationTemplate;
        action.notificationReceivers = notificationReceivers;
        action.delaySeconds = 0;
        action.isAsync = true;
        action.createdAt = LocalDateTime.now();
        action.updatedAt = LocalDateTime.now();
        return action;
    }

    /**
     * 更新动作配置
     */
    public void updateActionConfig(Map<String, Object> actionConfig) {
        this.actionConfig = actionConfig;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新动作顺序
     */
    public void updateActionOrder(Integer actionOrder) {
        this.actionOrder = actionOrder;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置延迟
     */
    public void setDelay(Integer delaySeconds) {
        this.delaySeconds = delaySeconds;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查是否异步执行
     */
    public boolean isAsyncExecution() {
        return this.isAsync != null && this.isAsync;
    }

    /**
     * 获取超时时间
     */
    public int getTimeout() {
        return this.timeoutSeconds != null ? this.timeoutSeconds : 30;
    }
}
