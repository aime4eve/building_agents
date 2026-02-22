-- ============================================================================
-- 华宽通智能体系统 - 规则引擎模块数据库初始化
-- 版本: V1.0
-- 创建日期: 2026-02-20
-- 说明: 规则引擎模块表结构
-- ============================================================================

USE `hkt_iot_rule`;

-- ============================================================================
-- 1. 规则表 (rule)
-- 说明: 规则主表，存储告警规则、联动规则、计费规则
-- ============================================================================
CREATE TABLE IF NOT EXISTS rule (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 规则标识
    rule_code             VARCHAR(100) NOT NULL COMMENT '规则编码',
    rule_name             VARCHAR(200) NOT NULL COMMENT '规则名称',

    -- 规则分类
    rule_type             VARCHAR(50) NOT NULL COMMENT '规则类型: ALARM/LINKAGE/BILLING/CONTROL',
    rule_category         VARCHAR(50) COMMENT '规则分类(业务维度)',

    -- 规则属性
    description           VARCHAR(1000) COMMENT '规则描述',
    rule_priority         INT DEFAULT 5 COMMENT '规则优先级(1-10,数字越大优先级越高)',

    -- 触发方式
    trigger_type          VARCHAR(50) NOT NULL COMMENT '触发方式: REALTIME/SCHEDULED/MANUAL',
    trigger_expression    TEXT COMMENT '触发表达式(JSON格式)',

    -- 规则内容(JSON格式)
    rule_config           JSON NOT NULL COMMENT '规则配置: {"conditions": [...], "actions": [...]}',
    rule_definition       JSON COMMENT '规则定义(DSL脚本)',

    -- 关联信息
    space_id              BIGINT UNSIGNED COMMENT '关联空间ID',
    device_ids            JSON COMMENT '关联设备ID列表: [id1, id2, ...]',

    -- 时间设置
    effective_time        DATETIME(3) COMMENT '生效时间',
    expire_time           DATETIME(3) COMMENT '失效时间(NULL表示永久)',

    -- 调度配置(定时规则)
    cron_expression        VARCHAR(100) COMMENT 'Cron表达式',
    schedule_config       JSON COMMENT '调度配置',

    -- 规则状态
    rule_status           VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '规则状态: DRAFT/ACTIVE/INACTIVE/ARCHIVED',
    is_enabled            TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用: 1-是 0-否',

    -- 执行统计
    total_executions      BIGINT DEFAULT 0 COMMENT '总执行次数',
    success_executions    BIGINT DEFAULT 0 COMMENT '成功执行次数',
    failed_executions     BIGINT DEFAULT 0 COMMENT '失败执行次数',
    last_execution_time   DATETIME(3) COMMENT '最后执行时间',
    last_execution_status VARCHAR(20) COMMENT '最后执行状态',

    -- 版本控制(乐观锁)
    version               BIGINT NOT NULL DEFAULT 0 COMMENT '版本号',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    created_by            BIGINT UNSIGNED COMMENT '创建人ID',
    updated_by            BIGINT UNSIGNED COMMENT '更新人ID',

    -- 软删除
    deleted               TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 1-已删除 0-正常',
    deleted_at            DATETIME(3) COMMENT '删除时间',
    deleted_by            BIGINT UNSIGNED COMMENT '删除人ID',

    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_rule_code (tenant_id, rule_code, deleted),
    KEY idx_tenant_id (tenant_id),
    KEY idx_rule_type (rule_type),
    KEY idx_rule_category (rule_category),
    KEY idx_rule_status (rule_status),
    KEY idx_is_enabled (is_enabled),
    KEY idx_trigger_type (trigger_type),
    KEY idx_space_id (space_id),
    KEY idx_last_execution_time (last_execution_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则表';


-- ============================================================================
-- 2. 规则条件表 (rule_condition)
-- 说明: 规则条件定义表
-- ============================================================================
CREATE TABLE IF NOT EXISTS rule_condition (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 关联规则
    rule_id               BIGINT UNSIGNED NOT NULL COMMENT '规则ID',
    rule_code             VARCHAR(100) NOT NULL COMMENT '规则编码',

    -- 条件信息
    condition_code        VARCHAR(100) NOT NULL COMMENT '条件编码',
    condition_name        VARCHAR(200) COMMENT '条件名称',

    -- 条件定义
    condition_type        VARCHAR(50) NOT NULL COMMENT '条件类型: DEVICE_PROPERTY/TIME_RANGE/EXPRESSION/COMPOSITE',
    condition_order       INT NOT NULL COMMENT '条件顺序',
    logic_operator        VARCHAR(10) NOT NULL DEFAULT 'AND' COMMENT '逻辑运算符: AND/OR/NOT',

    -- 条件参数(JSON格式)
    condition_config      JSON NOT NULL COMMENT '条件配置: {"deviceId":123,"property":"temp","operator":">","value":30}',

    -- 设备条件专用
    device_id             BIGINT UNSIGNED COMMENT '设备ID',
    property_identifier   VARCHAR(100) COMMENT '属性标识符',
    compare_operator      VARCHAR(20) COMMENT '比较运算符: >, <, =, >=, <=, !=, BETWEEN, IN',
    threshold_value       VARCHAR(200) COMMENT '阈值',

    -- 时间条件专用
    time_range_config     JSON COMMENT '时间范围配置: {"type":"WEEKDAY","start":"09:00","end":"18:00"}',

    -- 持续时间配置
    duration_threshold    INT COMMENT '持续时间阈值(秒)',
    duration_required     TINYINT DEFAULT 0 COMMENT '是否需要持续时间: 1-是 0-否',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    PRIMARY KEY (id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_rule_id (rule_id),
    KEY idx_condition_type (condition_type),
    KEY idx_device_id (device_id),
    KEY idx_condition_order (condition_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则条件表';


-- ============================================================================
-- 3. 规则动作表 (rule_action)
-- 说明: 规则动作定义表
-- ============================================================================
CREATE TABLE IF NOT EXISTS rule_action (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 关联规则
    rule_id               BIGINT UNSIGNED NOT NULL COMMENT '规则ID',
    rule_code             VARCHAR(100) NOT NULL COMMENT '规则编码',

    -- 动作信息
    action_code           VARCHAR(100) NOT NULL COMMENT '动作编码',
    action_name           VARCHAR(200) COMMENT '动作名称',

    -- 动作定义
    action_type           VARCHAR(50) NOT NULL COMMENT '动作类型: DEVICE_CONTROL/NOTIFICATION/WEBHOOK/WORKFLOW',
    action_order          INT NOT NULL COMMENT '动作顺序',
    delay_seconds         INT DEFAULT 0 COMMENT '延迟执行(秒)',

    -- 动作参数(JSON格式)
    action_config         JSON NOT NULL COMMENT '动作配置',

    -- 设备控制专用
    target_device_id      BIGINT UNSIGNED COMMENT '目标设备ID',
    service_identifier    VARCHAR(100) COMMENT '服务标识符',
    control_params        JSON COMMENT '控制参数: {"power": "on", "mode": "auto"}',

    -- 通知专用
    notification_channel  VARCHAR(50) COMMENT '通知渠道: SMS/EMAIL/WEB/APP',
    notification_template VARCHAR(100) COMMENT '通知模板',
    notification_receivers JSON COMMENT '通知接收人: [userId1, userId2]',

    -- Webhook专用
    webhook_url           VARCHAR(500) COMMENT 'Webhook URL',
    webhook_method        VARCHAR(10) DEFAULT 'POST' COMMENT 'HTTP方法',
    webhook_headers       JSON COMMENT 'HTTP头',
    retry_config          JSON COMMENT '重试配置',

    -- 工作流专用
    workflow_code         VARCHAR(100) COMMENT '工作流编码',
    workflow_params       JSON COMMENT '工作流参数',

    -- 执行配置
    is_async              TINYINT DEFAULT 1 COMMENT '是否异步执行: 1-是 0-否',
    timeout_seconds       INT DEFAULT 30 COMMENT '超时时间(秒)',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    PRIMARY KEY (id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_rule_id (rule_id),
    KEY idx_action_type (action_type),
    KEY idx_target_device_id (target_device_id),
    KEY idx_action_order (action_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则动作表';


-- ============================================================================
-- 4. 规则执行日志表 (rule_execution_log) - 分区表
-- 说明: 规则执行日志记录表
-- ============================================================================
CREATE TABLE IF NOT EXISTS rule_execution_log (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 关联规则
    rule_id               BIGINT UNSIGNED NOT NULL COMMENT '规则ID',
    rule_code             VARCHAR(100) NOT NULL COMMENT '规则编码',
    rule_type             VARCHAR(50) NOT NULL COMMENT '规则类型',

    -- 执行信息
    execution_id          VARCHAR(100) NOT NULL COMMENT '执行ID',
    trigger_type          VARCHAR(50) NOT NULL COMMENT '触发方式',
    trigger_source        VARCHAR(100) COMMENT '触发源',

    -- 触发数据(JSON格式)
    trigger_data          JSON COMMENT '触发数据',
    matched_conditions    JSON COMMENT '匹配的条件列表',

    -- 执行结果
    execution_status      VARCHAR(20) NOT NULL COMMENT '执行状态: SUCCESS/FAILED/PARTIAL/TIMEOUT',
    execution_result      JSON COMMENT '执行结果详情',
    error_message         TEXT COMMENT '错误信息',
    error_code            VARCHAR(50) COMMENT '错误码',

    -- 执行时间
    triggered_at          DATETIME(3) NOT NULL COMMENT '触发时间',
    started_at            DATETIME(3) COMMENT '开始执行时间',
    completed_at          DATETIME(3) COMMENT '完成时间',
    execution_duration    BIGINT COMMENT '执行时长(毫秒)',

    -- 动作执行统计
    total_actions         INT COMMENT '总动作数',
    success_actions       INT COMMENT '成功动作数',
    failed_actions        INT COMMENT '失败动作数',

    -- 执行上下文
    space_id              BIGINT UNSIGNED COMMENT '关联空间ID',
    device_ids            JSON COMMENT '相关设备ID列表',
    user_id               BIGINT UNSIGNED COMMENT '触发用户ID(NULL表示自动触发)',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

    PRIMARY KEY (id, triggered_at),
    UNIQUE KEY uk_execution_id (execution_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_rule_id (rule_id),
    KEY idx_rule_type (rule_type),
    KEY idx_execution_status (execution_status),
    KEY idx_triggered_at (triggered_at),
    KEY idx_status_triggered_at (execution_status, triggered_at),
    KEY idx_tenant_status (tenant_id, execution_status),
    KEY idx_space_id (space_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
PARTITION BY RANGE (TO_DAYS(triggered_at)) (
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    PARTITION p202604 VALUES LESS THAN (TO_DAYS('2026-05-01')),
    PARTITION p202605 VALUES LESS THAN (TO_DAYS('2026-06-01')),
    PARTITION p202606 VALUES LESS THAN (TO_DAYS('2026-07-01')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
) COMMENT='规则执行日志分区表';


-- ============================================================================
-- 5. 规则动作执行日志表 (rule_action_execution_log) - 分区表
-- 说明: 规则动作执行详细日志表
-- ============================================================================
CREATE TABLE IF NOT EXISTS rule_action_execution_log (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 关联执行记录
    rule_execution_id     BIGINT UNSIGNED NOT NULL COMMENT '规则执行日志ID',
    execution_id          VARCHAR(100) NOT NULL COMMENT '执行ID',

    -- 关联规则和动作
    rule_id               BIGINT UNSIGNED NOT NULL COMMENT '规则ID',
    rule_action_id        BIGINT UNSIGNED NOT NULL COMMENT '规则动作ID',
    action_code           VARCHAR(100) NOT NULL COMMENT '动作编码',
    action_type           VARCHAR(50) NOT NULL COMMENT '动作类型',

    -- 执行信息
    action_execution_id   VARCHAR(100) NOT NULL COMMENT '动作执行ID',
    action_order          INT NOT NULL COMMENT '动作顺序',

    -- 执行结果
    execution_status      VARCHAR(20) NOT NULL COMMENT '执行状态: PENDING/SUCCESS/FAILED/TIMEOUT',
    execution_result      JSON COMMENT '执行结果',
    error_message         TEXT COMMENT '错误信息',
    retry_count           INT DEFAULT 0 COMMENT '重试次数',

    -- 执行时间
    started_at            DATETIME(3) COMMENT '开始时间',
    completed_at          DATETIME(3) COMMENT '完成时间',
    execution_duration    BIGINT COMMENT '执行时长(毫秒)',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

    PRIMARY KEY (id, started_at),
    UNIQUE KEY uk_action_execution_id (action_execution_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_rule_execution_id (rule_execution_id),
    KEY idx_rule_id (rule_id),
    KEY idx_rule_action_id (rule_action_id),
    KEY idx_execution_status (execution_status),
    KEY idx_status_started_at (execution_status, started_at),
    KEY idx_started_at (started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
PARTITION BY RANGE (TO_DAYS(started_at)) (
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    PARTITION p202604 VALUES LESS THAN (TO_DAYS('2026-05-01')),
    PARTITION p202605 VALUES LESS THAN (TO_DAYS('2026-06-01')),
    PARTITION p202606 VALUES LESS THAN (TO_DAYS('2026-07-01')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
) COMMENT='规则动作执行日志分区表';


-- ============================================================================
-- 6. 规则变量表 (rule_variable)
-- 说明: 规则变量定义表，用于存储规则中可复用的变量
-- ============================================================================
CREATE TABLE IF NOT EXISTS rule_variable (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 关联规则
    rule_id               BIGINT UNSIGNED NOT NULL COMMENT '规则ID',

    -- 变量信息
    variable_code         VARCHAR(100) NOT NULL COMMENT '变量编码',
    variable_name         VARCHAR(200) COMMENT '变量名称',

    -- 变量定义
    variable_type         VARCHAR(50) NOT NULL COMMENT '变量类型: STRING/NUMBER/BOOLEAN/JSON/EXPRESSION',
    variable_value        TEXT COMMENT '变量值',
    default_value         TEXT COMMENT '默认值',

    -- 变量来源
    value_source          VARCHAR(50) COMMENT '值来源: STATIC/DYNAMIC/DEVICE/API',
    source_config         JSON COMMENT '来源配置',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_rule_variable (rule_id, variable_code),
    KEY idx_tenant_id (tenant_id),
    KEY idx_variable_code (variable_code),
    KEY idx_variable_type (variable_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='规则变量表';

-- 显示创建的表
SHOW TABLES;
SELECT 'Rule engine module tables created successfully!' AS status;
