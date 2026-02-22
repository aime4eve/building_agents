package com.hkt.iot.notification.infrastructure.persistence.po;

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
 * 通知请求PO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification_request")
public class NotificationRequestPO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("dedupe_key")
    private String dedupeKey;

    @TableField("tenant_id")
    private String tenantId;

    @TableField("channel_type")
    private String channelType;

    @TableField("receiver_type")
    private String receiverType;

    @TableField("receiver_id")
    private String receiverId;

    @TableField("receiver_address")
    private String receiverAddress;

    @TableField("template_code")
    private String templateCode;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("variables")
    private String variables;

    @TableField("priority")
    private String priority;

    @TableField("status")
    private String status;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("max_retry")
    private Integer maxRetry;

    @TableField("next_retry_at")
    private Instant nextRetryAt;

    @TableField("error_message")
    private String errorMessage;

    @TableField("business_type")
    private String businessType;

    @TableField("business_id")
    private String businessId;

    @TableField("correlation_id")
    private String correlationId;

    @TableField("scheduled_at")
    private Instant scheduledAt;

    @TableField("sent_at")
    private Instant sentAt;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("updated_at")
    private Instant updatedAt;
}
