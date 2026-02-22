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
import java.util.Map;

/**
 * 通知请求聚合根
 * 用于管理通知发送请求和状态
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification_request")
public class NotificationRequest {

    /**
     * 请求ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 幂等键（用于去重）
     */
    @TableField("dedupe_key")
    private String dedupeKey;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private String tenantId;

    /**
     * 通知渠道
     */
    @TableField("channel_type")
    private NotificationTemplate.ChannelType channelType;

    /**
     * 接收者类型: USER用户, ROLE角色, GROUP组
     */
    @TableField("receiver_type")
    private ReceiverType receiverType;

    /**
     * 接收者ID
     */
    @TableField("receiver_id")
    private String receiverId;

    /**
     * 接收者地址（邮箱/手机号/设备ID）
     */
    @TableField("receiver_address")
    private String receiverAddress;

    /**
     * 模板编码
     */
    @TableField("template_code")
    private String templateCode;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 模板变量（JSON格式）
     */
    @TableField("variables")
    private String variables;

    /**
     * 优先级: LOW低, NORMAL普通, HIGH高, URGENT紧急
     */
    @TableField("priority")
    private Priority priority;

    /**
     * 发送状态: PENDING待发送, SENDING发送中, SUCCESS成功, FAILED失败, CANCELLED已取消
     */
    @TableField("status")
    private NotificationStatus status;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    @TableField("max_retry")
    private Integer maxRetry;

    /**
     * 下次重试时间
     */
    @TableField("next_retry_at")
    private Instant nextRetryAt;

    /**
     * 失败原因
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 关联业务类型
     */
    @TableField("business_type")
    private String businessType;

    /**
     * 关联业务ID
     */
    @TableField("business_id")
    private String businessId;

    /**
     * CorrelationID用于链路追踪
     */
    @TableField("correlation_id")
    private String correlationId;

    /**
     * 预定发送时间
     */
    @TableField("scheduled_at")
    private Instant scheduledAt;

    /**
     * 实际发送时间
     */
    @TableField("sent_at")
    private Instant sentAt;

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
     * 接收者类型枚举
     */
    public enum ReceiverType {
        /**
         * 用户
         */
        USER("USER", "用户"),
        /**
         * 角色
         */
        ROLE("ROLE", "角色"),
        /**
         * 用户组
         */
        GROUP("GROUP", "用户组"),
        /**
         * 设备
         */
        DEVICE("DEVICE", "设备");

        private final String code;
        private final String description;

        ReceiverType(String code, String description) {
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
     * 优先级枚举
     */
    public enum Priority {
        /**
         * 低优先级
         */
        LOW(1, "低"),
        /**
         * 普通优先级
         */
        NORMAL(2, "普通"),
        /**
         * 高优先级
         */
        HIGH(3, "高"),
        /**
         * 紧急优先级
         */
        URGENT(4, "紧急");

        private final Integer level;
        private final String description;

        Priority(Integer level, String description) {
            this.level = level;
            this.description = description;
        }

        public Integer getLevel() {
            return level;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 通知状态枚举
     */
    public enum NotificationStatus {
        /**
         * 待发送
         */
        PENDING("PENDING", "待发送"),
        /**
         * 发送中
         */
        SENDING("SENDING", "发送中"),
        /**
         * 发送成功
         */
        SUCCESS("SUCCESS", "发送成功"),
        /**
         * 发送失败
         */
        FAILED("FAILED", "发送失败"),
        /**
         * 已取消
         */
        CANCELLED("CANCELLED", "已取消"),
        /**
         * 死信（超过最大重试次数）
         */
        DEAD_LETTER("DEAD_LETTER", "死信");

        private final String code;
        private final String description;

        NotificationStatus(String code, String description) {
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
     * 开始发送
     */
    public void startSending() {
        this.status = NotificationStatus.SENDING;
        this.updatedAt = Instant.now();
    }

    /**
     * 标记发送成功
     */
    public void markAsSuccess() {
        this.status = NotificationStatus.SUCCESS;
        this.sentAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 标记发送失败
     */
    public void markAsFailed(String errorMessage) {
        this.errorMessage = errorMessage;
        if (this.retryCount >= this.maxRetry) {
            this.status = NotificationStatus.FAILED;
        } else {
            this.status = NotificationStatus.PENDING;
            calculateNextRetryTime();
        }
        this.updatedAt = Instant.now();
    }

    /**
     * 计算下次重试时间（指数退避）
     */
    private void calculateNextRetryTime() {
        long delaySeconds = (long) Math.pow(2, this.retryCount) * 60;
        this.nextRetryAt = Instant.now().plusSeconds(delaySeconds);
        this.retryCount++;
    }

    /**
     * 取消发送
     */
    public void cancel() {
        this.status = NotificationStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    /**
     * 检查是否可以重试
     */
    public boolean canRetry() {
        return this.status == NotificationStatus.PENDING &&
                this.retryCount < this.maxRetry &&
                (this.nextRetryAt == null || this.nextRetryAt.isBefore(Instant.now()));
    }

    /**
     * 标记为死信
     */
    public void markAsDeadLetter() {
        this.status = NotificationStatus.DEAD_LETTER;
        this.updatedAt = Instant.now();
    }

    /**
     * 重置以允许重试（从死信状态恢复）
     */
    public void resetForRetry() {
        this.status = NotificationStatus.PENDING;
        this.retryCount = 0;
        this.nextRetryAt = null;
        this.errorMessage = null;
        this.updatedAt = Instant.now();
    }

    /**
     * 获取最后一次错误信息
     */
    public String getLastError() {
        return this.errorMessage;
    }
}
