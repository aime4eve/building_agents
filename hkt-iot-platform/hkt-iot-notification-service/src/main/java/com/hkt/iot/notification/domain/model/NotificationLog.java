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
 * 通知日志实体
 * 记录所有通知发送历史
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification_log")
public class NotificationLog {

    /**
     * 日志ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联请求ID
     */
    @TableField("request_id")
    private Long requestId;

    /**
     * 幂等键
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
     * 接收者类型
     */
    @TableField("receiver_type")
    private NotificationRequest.ReceiverType receiverType;

    /**
     * 接收者ID
     */
    @TableField("receiver_id")
    private String receiverId;

    /**
     * 接收者地址
     */
    @TableField("receiver_address")
    private String receiverAddress;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 内容摘要
     */
    @TableField("content_summary")
    private String contentSummary;

    /**
     * 发送状态
     */
    @TableField("status")
    private NotificationRequest.NotificationStatus status;

    /**
     * 响应码
     */
    @TableField("response_code")
    private String responseCode;

    /**
     * 响应消息
     */
    @TableField("response_message")
    private String responseMessage;

    /**
     * 第三方消息ID（用于追踪）
     */
    @TableField("external_message_id")
    private String externalMessageId;

    /**
     * CorrelationID
     */
    @TableField("correlation_id")
    private String correlationId;

    /**
     * 发送时间
     */
    @TableField("sent_at")
    private Instant sentAt;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private Instant createdAt;

    /**
     * 创建日志
     */
    public static NotificationLog create(NotificationRequest request) {
        return NotificationLog.builder()
                .requestId(request.getId())
                .dedupeKey(request.getDedupeKey())
                .tenantId(request.getTenantId())
                .channelType(request.getChannelType())
                .receiverType(request.getReceiverType())
                .receiverId(request.getReceiverId())
                .receiverAddress(request.getReceiverAddress())
                .title(request.getTitle())
                .contentSummary(truncateContent(request.getContent()))
                .status(request.getStatus())
                .correlationId(request.getCorrelationId())
                .createdAt(Instant.now())
                .build();
    }

    /**
     * 更新发送结果
     */
    public void updateResult(String responseCode, String responseMessage, String externalMessageId) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.externalMessageId = externalMessageId;
        this.sentAt = Instant.now();
    }

    /**
     * 截断内容
     */
    private static String truncateContent(String content) {
        if (content == null) {
            return null;
        }
        return content.length() > 200 ? content.substring(0, 200) + "..." : content;
    }
}
