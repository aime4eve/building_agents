-- 通知中心服务数据库初始化脚本
-- 创建时间: 2026-02-21

-- 创建数据库
CREATE DATABASE IF NOT EXISTS hkt_iot_notification DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hkt_iot_notification;

-- 通知模板表
CREATE TABLE IF NOT EXISTS notification_template (
    id BIGINT PRIMARY KEY COMMENT '模板ID',
    template_code VARCHAR(64) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(128) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(32) NOT NULL COMMENT '模板类型: ALARM/SYSTEM/BUSINESS',
    channel_type VARCHAR(32) NOT NULL COMMENT '通知渠道: PUSH/EMAIL/SMS/IN_APP/WEBHOOK',
    title_template VARCHAR(512) COMMENT '模板标题',
    content_template TEXT NOT NULL COMMENT '模板内容',
    variables TEXT COMMENT '模板变量定义(JSON格式)',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_template_code (template_code),
    KEY idx_tenant_id (tenant_id),
    KEY idx_type_channel (template_type, channel_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- 通知请求表
CREATE TABLE IF NOT EXISTS notification_request (
    id BIGINT PRIMARY KEY COMMENT '请求ID',
    dedupe_key VARCHAR(256) COMMENT '幂等键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    channel_type VARCHAR(32) NOT NULL COMMENT '通知渠道',
    receiver_type VARCHAR(32) NOT NULL COMMENT '接收者类型',
    receiver_id VARCHAR(128) NOT NULL COMMENT '接收者ID',
    receiver_address VARCHAR(512) COMMENT '接收者地址',
    template_code VARCHAR(64) NOT NULL COMMENT '模板编码',
    title VARCHAR(512) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    variables TEXT COMMENT '模板变量(JSON格式)',
    priority VARCHAR(16) NOT NULL DEFAULT 'NORMAL' COMMENT '优先级: LOW/NORMAL/HIGH/URGENT',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '发送状态',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    max_retry INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    next_retry_at TIMESTAMP NULL COMMENT '下次重试时间',
    error_message TEXT COMMENT '失败原因',
    business_type VARCHAR(64) COMMENT '关联业务类型',
    business_id VARCHAR(128) COMMENT '关联业务ID',
    correlation_id VARCHAR(64) COMMENT 'CorrelationID',
    scheduled_at TIMESTAMP NULL COMMENT '预定发送时间',
    sent_at TIMESTAMP NULL COMMENT '实际发送时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_dedupe_key (dedupe_key),
    KEY idx_tenant_id (tenant_id),
    KEY idx_status (status),
    KEY idx_next_retry (next_retry_at),
    KEY idx_correlation (correlation_id),
    KEY idx_business (business_type, business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知请求表';

-- 通知日志表
CREATE TABLE IF NOT EXISTS notification_log (
    id BIGINT PRIMARY KEY COMMENT '日志ID',
    request_id BIGINT NOT NULL COMMENT '关联请求ID',
    dedupe_key VARCHAR(256) COMMENT '幂等键',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    channel_type VARCHAR(32) NOT NULL COMMENT '通知渠道',
    receiver_type VARCHAR(32) NOT NULL COMMENT '接收者类型',
    receiver_id VARCHAR(128) NOT NULL COMMENT '接收者ID',
    receiver_address VARCHAR(512) COMMENT '接收者地址',
    title VARCHAR(512) COMMENT '标题',
    content_summary VARCHAR(500) COMMENT '内容摘要',
    status VARCHAR(16) NOT NULL COMMENT '发送状态',
    response_code VARCHAR(64) COMMENT '响应码',
    response_message TEXT COMMENT '响应消息',
    external_message_id VARCHAR(256) COMMENT '第三方消息ID',
    correlation_id VARCHAR(64) COMMENT 'CorrelationID',
    sent_at TIMESTAMP NULL COMMENT '发送时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_request_id (request_id),
    KEY idx_tenant_id (tenantId),
    KEY idx_dedupe_key (dedupe_key),
    KEY idx_correlation_id (correlation_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知日志表';

-- 插入默认模板
INSERT INTO notification_template (id, template_code, template_name, template_type, channel_type, title_template, content_template, tenant_id, enabled) VALUES
(1, 'ALARM_TRIGGERED', '告警触发通知', 'ALARM', 'EMAIL', '【华宽通智能体】告警通知', '尊敬的用户，您的设备 ${deviceName} 于 ${time} 触发了 ${alarmLevel} 级告警：${alarmMessage}', 'default', TRUE),
(2, 'ALARM_TRIGGERED_PUSH', '告警推送通知', 'ALARM', 'PUSH', '告警通知', '您的设备 ${deviceName} 触发了 ${alarmLevel} 级告警', 'default', TRUE),
(3, 'SYSTEM_NOTICE', '系统通知', 'SYSTEM', 'IN_APP', '系统通知', '${content}', 'default', TRUE),
(4, 'WELCOME', '欢迎通知', 'BUSINESS', 'EMAIL', '欢迎使用华宽通智能体', '尊敬的 ${userName}，欢迎您使用华宽通智能体平台！', 'default', TRUE);

-- 创建索引优化查询性能
CREATE INDEX idx_template_type_channel ON notification_template(template_type, channel_type);
CREATE INDEX idx_request_status_tenant ON notification_request(status, tenant_id);
CREATE INDEX idx_log_tenant_time ON notification_log(tenant_id, created_at);
