package com.hkt.iot.notification.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 通知模板聚合根
 * 用于管理各类通知消息的模板
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification_template")
public class NotificationTemplate {

    /**
     * 模板ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模板编码
     */
    @TableField("template_code")
    private String templateCode;

    /**
     * 模板名称
     */
    @TableField("template_name")
    private String templateName;

    /**
     * 模板类型: ALARM告警, SYSTEM系统, BUSINESS业务
     */
    @TableField("template_type")
    private TemplateType templateType;

    /**
     * 通知渠道: PUSH/EMAIL/SMS/IN_APP/WEBHOOK
     */
    @TableField("channel_type")
    private ChannelType channelType;

    /**
     * 模板标题
     */
    @TableField("title_template")
    private String titleTemplate;

    /**
     * 模板内容
     */
    @TableField("content_template")
    private String contentTemplate;

    /**
     * 模板变量（JSON格式）
     */
    @TableField("variables")
    private String variables;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private Instant createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private Instant updatedAt;

    /**
     * 模板类型枚举
     */
    public enum TemplateType {
        /**
         * 告警通知
         */
        ALARM("ALARM", "告警通知"),
        /**
         * 系统通知
         */
        SYSTEM("SYSTEM", "系统通知"),
        /**
         * 业务通知
         */
        BUSINESS("BUSINESS", "业务通知");

        private final String code;
        private final String description;

        TemplateType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 通知渠道类型枚举
     */
    public enum ChannelType {
        /**
         * APP推送
         */
        PUSH("PUSH", "APP推送"),
        /**
         * 邮件
         */
        EMAIL("EMAIL", "邮件"),
        /**
         * 短信
         */
        SMS("SMS", "短信"),
        /**
         * 站内信
         */
        IN_APP("IN_APP", "站内信"),
        /**
         * Webhook
         */
        WEBHOOK("WEBHOOK", "Webhook");

        private final String code;
        private final String description;

        ChannelType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 启用模板
     */
    public void enable() {
        this.enabled = true;
        this.updatedAt = Instant.now();
    }

    /**
     * 禁用模板
     */
    public void disable() {
        this.enabled = false;
        this.updatedAt = Instant.now();
    }

    /**
     * 更新模板内容
     */
    public void updateContent(String titleTemplate, String contentTemplate, String variables) {
        this.titleTemplate = titleTemplate;
        this.contentTemplate = contentTemplate;
        this.variables = variables;
        this.updatedAt = Instant.now();
    }
}
