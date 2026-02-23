-- 华宽通智能体平台 - 工作流引擎服务数据库表结构
-- 数据库：hkt_iot_workflow
-- 创建日期：2026-02-22

-- ============================================
--  Camunda BPM 核心表（由 Camunda 自动创建）
--  ACT_RE_* : Repository 表，存储流程定义等静态信息
--  ACT_RU_* : Runtime 表，存储运行时信息（流程实例、任务等）
--  ACT_HI_* : History 表，存储历史信息
--  ACT_GE_* : General 表，存储通用信息
--  ACT_ID_* : Identity 表，存储用户、组等信息
--  ACT_PROCDEF_* : 流程定义表
-- ============================================

-- ============================================
--  业务扩展表
-- ============================================

-- 流程实例表
CREATE TABLE IF NOT EXISTS `wf_process_instance` (
    `id` VARCHAR(64) NOT NULL COMMENT '流程实例 ID',
    `business_key` VARCHAR(255) NOT NULL COMMENT '业务键',
    `process_definition_key` VARCHAR(255) NOT NULL COMMENT '流程定义键',
    `state` VARCHAR(20) NOT NULL COMMENT '流程状态 (STARTED/RUNNING/SUSPENDED/COMPLETED/FAILED/CANCELLED)',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户 ID',
    `started_by` VARCHAR(64) NOT NULL COMMENT '启动人 ID',
    `current_activity_id` VARCHAR(255) COMMENT '当前活动 ID',
    `started_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    `ended_at` TIMESTAMP COMMENT '结束时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version` BIGINT DEFAULT 0 COMMENT '版本号（乐观锁）',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_business_key` (`business_key`),
    KEY `idx_tenant_process` (`tenant_id`, `process_definition_key`),
    KEY `idx_state` (`state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例表';

-- 工作流任务表
CREATE TABLE IF NOT EXISTS `wf_task` (
    `id` VARCHAR(64) NOT NULL COMMENT '任务 ID',
    `process_instance_id` VARCHAR(64) NOT NULL COMMENT '流程实例 ID',
    `task_definition_key` VARCHAR(255) NOT NULL COMMENT '任务定义键',
    `task_name` VARCHAR(255) NOT NULL COMMENT '任务名称',
    `task_type` VARCHAR(20) NOT NULL COMMENT '任务类型 (USER_TASK/SERVICE_TASK/SCRIPT_TASK/MANUAL_TASK)',
    `status` VARCHAR(20) NOT NULL COMMENT '任务状态 (PENDING/IN_PROGRESS/COMPLETED/CANCELLED/FAILED)',
    `assignee` VARCHAR(64) COMMENT '处理人 ID',
    `candidate_groups` TEXT COMMENT '候选组 (JSON 数组)',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `due_date` TIMESTAMP COMMENT '到期时间',
    `completed_at` TIMESTAMP COMMENT '完成时间',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户 ID',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `version` BIGINT DEFAULT 0 COMMENT '版本号（乐观锁）',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志',
    PRIMARY KEY (`id`),
    KEY `idx_process_instance` (`process_instance_id`),
    KEY `idx_assignee` (`assignee`),
    KEY `idx_status` (`status`),
    KEY `idx_tenant_process` (`tenant_id`, `process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流任务表';

-- SLA 配置表
CREATE TABLE IF NOT EXISTS `wf_sla_config` (
    `id` VARCHAR(64) NOT NULL COMMENT 'SLA 配置 ID',
    `process_definition_key` VARCHAR(255) NOT NULL COMMENT '流程定义键',
    `task_definition_key` VARCHAR(255) COMMENT '任务定义键',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户 ID',
    `response_time_limit` BIGINT NOT NULL COMMENT '响应时间限制 (秒)',
    `resolution_time_limit` BIGINT NOT NULL COMMENT '解决时间限制 (秒)',
    `priority` VARCHAR(20) COMMENT '优先级 (LOW/MEDIUM/HIGH/CRITICAL)',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志',
    PRIMARY KEY (`id`),
    KEY `idx_tenant_process` (`tenant_id`, `process_definition_key`),
    KEY `idx_process_task` (`process_definition_key`, `task_definition_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SLA 配置表';

-- SLA 监控记录表
CREATE TABLE IF NOT EXISTS `wf_sla_monitor` (
    `id` VARCHAR(64) NOT NULL COMMENT 'SLA 监控 ID',
    `process_instance_id` VARCHAR(64) NOT NULL COMMENT '流程实例 ID',
    `task_id` VARCHAR(64) COMMENT '任务 ID',
    `sla_config_id` VARCHAR(64) NOT NULL COMMENT 'SLA 配置 ID',
    `sla_deadline` TIMESTAMP NOT NULL COMMENT 'SLA 截止时间',
    `response_status` VARCHAR(20) NOT NULL COMMENT '响应 SLA 状态 (PENDING/COMPLIANT/WARNING/BREACHED)',
    `resolution_status` VARCHAR(20) COMMENT '解决 SLA 状态 (PENDING/COMPLIANT/BREACHED)',
    `actual_response_time` TIMESTAMP COMMENT '实际响应时间',
    `actual_resolution_time` TIMESTAMP COMMENT '实际解决时间',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `tenant_id` VARCHAR(64) NOT NULL COMMENT '租户 ID',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标志',
    PRIMARY KEY (`id`),
    KEY `idx_process_instance` (`process_instance_id`),
    KEY `idx_task` (`task_id`),
    KEY `idx_tenant_process` (`tenant_id`, `process_instance_id`),
    KEY `idx_response_status` (`response_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SLA 监控记录表';

-- ============================================
--  初始化数据
-- ============================================

-- 插入默认 SLA 配置
INSERT INTO `wf_sla_config` (`id`, `process_definition_key`, `task_definition_key`, `tenant_id`, `response_time_limit`, `resolution_time_limit`, `priority`, `created_at`, `updated_at`, `deleted`)
VALUES
    ('sla-config-001', 'property-repair-workorder', 'auto-assign', 'tenant-001', 1800, 28800, 'HIGH', NOW(), NOW(), 0),
    ('sla-config-002', 'property-repair-workorder', 'repair-processing', 'tenant-001', 1800, 14400, 'MEDIUM', NOW(), NOW(), 0),
    ('sla-config-003', 'lease-approval-process', 'manager-approval', 'tenant-001', 3600, 7200, 'HIGH', NOW(), NOW(), 0);

-- ============================================
--  视图：SLA 达成率统计
-- ============================================

CREATE OR REPLACE VIEW `v_sla_statistics` AS
SELECT
    tenant_id,
    COUNT(*) AS total_count,
    SUM(CASE WHEN response_status = 'COMPLIANT' THEN 1 ELSE 0 END) AS compliant_count,
    SUM(CASE WHEN response_status = 'BREACHED' THEN 1 ELSE 0 END) AS breached_count,
    SUM(CASE WHEN response_status = 'WARNING' THEN 1 ELSE 0 END) AS warning_count,
    ROUND(SUM(CASE WHEN response_status = 'COMPLIANT' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) AS compliance_rate
FROM `wf_sla_monitor`
WHERE deleted = 0
GROUP BY tenant_id;
