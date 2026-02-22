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
 * 通知日志PO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification_log")
public class NotificationLogPO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("request_id")
    private Long requestId;

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

    @TableField("title")
    private String title;

    @TableField("content_summary")
    private String contentSummary;

    @TableField("status")
    private String status;

    @TableField("response_code")
    private String responseCode;

    @TableField("response_message")
    private String responseMessage;

    @TableField("external_message_id")
    private String externalMessageId;

    @TableField("correlation_id")
    private String correlationId;

    @TableField("sent_at")
    private Instant sentAt;

    @TableField("created_at")
    private Instant createdAt;
}
