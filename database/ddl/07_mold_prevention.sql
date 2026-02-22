-- ============================================================================
-- 华宽通智能体系统 - 防霉管控模块数据库设计
-- 版本：V1.0
-- 数据库：MySQL 8.0+
-- 创建日期：2026-02-22
-- ============================================================================

-- ============================================================================
-- 1. 防霉管控区域表 (mold_prevention_zone)
-- 说明：管理防霉管控区域的定义、风险监测和湿度控制
-- ============================================================================
CREATE TABLE mold_prevention_zone (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    
    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户 ID',
    
    -- 区域标识
    zone_code             VARCHAR(100) NOT NULL COMMENT '区域编码',
    zone_name             VARCHAR(200) NOT NULL COMMENT '区域名称',
    
    -- 关联空间
    space_id              BIGINT UNSIGNED NOT NULL COMMENT '关联空间 ID',
    
    -- 区域状态
    zone_status           VARCHAR(20) NOT NULL DEFAULT 'INACTIVE' COMMENT '区域状态：ACTIVE/INACTIVE/MAINTENANCE',
    
    -- 风险阈值配置 (JSON)
    risk_threshold        JSON COMMENT '风险阈值配置：{"humidityLow": 55, "humidityHigh": 65}',
    
    -- 湿度控制策略 (JSON)
    control_strategy      JSON COMMENT '湿度控制策略配置',
    
    -- 当前风险等级
    current_risk_level    VARCHAR(20) DEFAULT 'LOW' COMMENT '当前风险等级：LOW/MEDIUM/HIGH/CRITICAL',
    
    -- 最后环境数据 (JSON)
    last_environment_data JSON COMMENT '最后环境数据：{"temperature": 25.5, "humidity": 60.0, "timestamp": "..."}',
    
    -- 最后评估时间
    last_evaluated_at     DATETIME(3) COMMENT '最后风险评估时间',
    
    -- 描述信息
    description           VARCHAR(500) COMMENT '区域描述',
    
    -- 版本控制 (乐观锁)
    version               BIGINT NOT NULL DEFAULT 0 COMMENT '版本号',
    
    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    created_by            BIGINT UNSIGNED COMMENT '创建人 ID',
    updated_by            BIGINT UNSIGNED COMMENT '更新人 ID',
    
    -- 软删除
    deleted               TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：1-已删除 0-正常',
    deleted_at            DATETIME(3) COMMENT '删除时间',
    deleted_by            BIGINT UNSIGNED COMMENT '删除人 ID',
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_zone_code (tenant_id, zone_code, deleted),
    KEY idx_tenant_id (tenant_id),
    KEY idx_space_id (space_id),
    KEY idx_zone_status (zone_status),
    KEY idx_current_risk_level (current_risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='防霉管控区域表';


-- ============================================================================
-- 2. 防霉效果报告表 (mold_prevention_report)
-- 说明：记录防霉管控的效果分析和统计数据 (独立聚合根，历史记录)
-- ============================================================================
CREATE TABLE mold_prevention_report (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    
    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户 ID',
    
    -- 关联区域
    zone_id               BIGINT UNSIGNED NOT NULL COMMENT '防霉管控区域 ID',
    
    -- 报告周期
    report_period         VARCHAR(20) NOT NULL COMMENT '报告周期：DAILY/WEEKLY/MONTHLY',
    period_year           INT NOT NULL COMMENT '周期年份',
    period_month          INT NOT NULL COMMENT '周期月份',
    period_day            INT COMMENT '周期日 (日报时使用)',
    
    -- 数据采集时间范围
    collected_from        DATETIME(3) NOT NULL COMMENT '数据采集开始时间',
    collected_to          DATETIME(3) NOT NULL COMMENT '数据采集结束时间',
    
    -- 预测准确率
    prediction_accuracy   DECIMAL(5,4) COMMENT '预测准确率 (0-1 之间)',
    total_predictions     INT DEFAULT 0 COMMENT '总预测次数',
    correct_predictions   INT DEFAULT 0 COMMENT '正确预测次数',
    
    -- 风险统计 (JSON)
    risk_statistics       JSON COMMENT '风险统计：{"lowCount": 10, "mediumCount": 5, "highCount": 2, "criticalCount": 1}',
    
    -- 湿度统计 (JSON)
    humidity_statistics   JSON COMMENT '湿度统计：{"avg": 60.5, "min": 45.0, "max": 80.0}',
    
    -- 控制效果评估 (JSON)
    control_effectiveness JSON COMMENT '控制效果评估：{"score": 85.5, "targetHumidity": 55.0, "currentHumidity": 58.0}',
    
    -- 霉菌事件数量
    incident_count        INT DEFAULT 0 COMMENT '霉菌事件数量',
    
    -- 报告生成信息
    status                VARCHAR(20) NOT NULL DEFAULT 'GENERATING' COMMENT '报告状态：GENERATING/COMPLETED/FAILED',
    started_at            DATETIME(3) COMMENT '开始生成时间',
    completed_at          DATETIME(3) COMMENT '完成生成时间',
    generation_time_ms    BIGINT COMMENT '生成耗时 (毫秒)',
    error_message         VARCHAR(1000) COMMENT '错误信息 (失败时)',
    
    -- 导出信息
    export_format         VARCHAR(20) COMMENT '导出格式：PDF/EXCEL',
    export_url            VARCHAR(500) COMMENT '导出文件 URL',
    
    -- 版本控制 (乐观锁)
    version               BIGINT NOT NULL DEFAULT 0 COMMENT '版本号',
    
    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    created_by            BIGINT UNSIGNED COMMENT '创建人 ID',
    updated_by            BIGINT UNSIGNED COMMENT '更新人 ID',
    
    PRIMARY KEY (id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_zone_id (zone_id),
    KEY idx_report_period (report_period, period_year, period_month),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='防霉效果报告表';


-- ============================================================================
-- 3. 防霉传感器配置表 (mold_prevention_sensor)
-- 说明：记录防霉管控区域配置的传感器设备
-- ============================================================================
CREATE TABLE mold_prevention_sensor (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    
    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户 ID',
    
    -- 关联区域
    zone_id               BIGINT UNSIGNED NOT NULL COMMENT '防霉管控区域 ID',
    
    -- 传感器设备 ID
    sensor_device_id      BIGINT UNSIGNED NOT NULL COMMENT '传感器设备 ID',
    sensor_device_sn      VARCHAR(100) NOT NULL COMMENT '传感器设备序列号',
    
    -- 传感器类型
    sensor_type           VARCHAR(50) NOT NULL COMMENT '传感器类型：TEMPERATURE/HUMIDITY/COMBINED',
    
    -- 传感器位置
    location_detail       VARCHAR(500) COMMENT '传感器位置详情',
    
    -- 在线状态
    online                TINYINT NOT NULL DEFAULT 1 COMMENT '在线状态：1-在线 0-离线',
    
    -- 校准信息 (JSON)
    calibration_info      JSON COMMENT '校准信息：{"lastCalibratedAt": "...", "offset": 0.5}',
    
    -- 版本控制 (乐观锁)
    version               BIGINT NOT NULL DEFAULT 0 COMMENT '版本号',
    
    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    created_by            BIGINT UNSIGNED COMMENT '创建人 ID',
    updated_by            BIGINT UNSIGNED COMMENT '更新人 ID',
    
    -- 软删除
    deleted               TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：1-已删除 0-正常',
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_zone_sensor (zone_id, sensor_device_id, deleted),
    KEY idx_tenant_id (tenant_id),
    KEY idx_zone_id (zone_id),
    KEY idx_sensor_type (sensor_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='防霉传感器配置表';


-- ============================================================================
-- 4. 防霉控制器配置表 (mold_prevention_controller)
-- 说明：记录防霉管控区域配置的控制器设备 (除湿机、新风机等)
-- ============================================================================
CREATE TABLE mold_prevention_controller (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    
    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户 ID',
    
    -- 关联区域
    zone_id               BIGINT UNSIGNED NOT NULL COMMENT '防霉管控区域 ID',
    
    -- 控制器设备 ID
    controller_device_id  BIGINT UNSIGNED NOT NULL COMMENT '控制器设备 ID',
    controller_device_sn  VARCHAR(100) NOT NULL COMMENT '控制器设备序列号',
    
    -- 控制器类型
    controller_type       VARCHAR(50) NOT NULL COMMENT '控制器类型：DEHUMIDIFIER/AIR_CONDITIONER/VENTILATOR',
    
    -- 控制器位置
    location_detail       VARCHAR(500) COMMENT '控制器位置详情',
    
    -- 在线状态
    online                TINYINT NOT NULL DEFAULT 1 COMMENT '在线状态：1-在线 0-离线',
    
    -- 版本控制 (乐观锁)
    version               BIGINT NOT NULL DEFAULT 0 COMMENT '版本号',
    
    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    created_by            BIGINT UNSIGNED COMMENT '创建人 ID',
    updated_by            BIGINT UNSIGNED COMMENT '更新人 ID',
    
    -- 软删除
    deleted               TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：1-已删除 0-正常',
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_zone_controller (zone_id, controller_device_id, deleted),
    KEY idx_tenant_id (tenant_id),
    KEY idx_zone_id (zone_id),
    KEY idx_controller_type (controller_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='防霉控制器配置表';


-- ============================================================================
-- 5. 霉菌风险历史记录表 (mold_risk_history)
-- 说明：记录防霉管控区域的历史风险等级变化
-- ============================================================================
CREATE TABLE mold_risk_history (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    
    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户 ID',
    
    -- 关联区域
    zone_id               BIGINT UNSIGNED NOT NULL COMMENT '防霉管控区域 ID',
    
    -- 风险等级
    risk_level            VARCHAR(20) NOT NULL COMMENT '风险等级：LOW/MEDIUM/HIGH/CRITICAL',
    previous_risk_level   VARCHAR(20) COMMENT '上一个风险等级',
    
    -- 环境数据
    temperature           DECIMAL(5,2) COMMENT '温度 (摄氏度)',
    humidity              DECIMAL(5,2) COMMENT '湿度 (百分比)',
    
    -- 风险评分
    risk_score            INT COMMENT '风险评分 (0-100)',
    
    -- 评估时间
    evaluated_at          DATETIME(3) NOT NULL COMMENT '评估时间',
    
    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    
    PRIMARY KEY (id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_zone_id (zone_id),
    KEY idx_risk_level (risk_level),
    KEY idx_evaluated_at (evaluated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='霉菌风险历史记录表';


-- ============================================================================
-- 6. 控制命令历史表 (mold_control_command_history)
-- 说明：记录发送给控制器设备的命令历史
-- ============================================================================
CREATE TABLE mold_control_command_history (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    
    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户 ID',
    
    -- 关联区域
    zone_id               BIGINT UNSIGNED NOT NULL COMMENT '防霉管控区域 ID',
    
    -- 关联控制器
    controller_id         BIGINT UNSIGNED NOT NULL COMMENT '控制器 ID',
    
    -- 命令类型
    command_type          VARCHAR(50) NOT NULL COMMENT '命令类型：TURN_ON/TURN_OFF/SET_MODE/SET_TARGET',
    
    -- 命令参数 (JSON)
    command_params        JSON COMMENT '命令参数：{"mode": "AUTO", "targetHumidity": 55}',
    
    -- 命令状态
    command_status        VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '命令状态：PENDING/SENT/SUCCESS/FAILED',
    
    -- 执行结果
    result_message        VARCHAR(500) COMMENT '执行结果消息',
    
    -- 重试次数
    retry_count           INT DEFAULT 0 COMMENT '重试次数',
    
    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    sent_at               DATETIME(3) COMMENT '发送时间',
    completed_at          DATETIME(3) COMMENT '完成时间',
    created_by            BIGINT UNSIGNED COMMENT '创建人 ID',
    
    PRIMARY KEY (id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_zone_id (zone_id),
    KEY idx_controller_id (controller_id),
    KEY idx_command_status (command_status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='控制命令历史表';


-- ============================================================================
-- 7. 霉菌事件表 (mold_incident)
-- 说明：记录霉菌风险事件 (用于报告统计)
-- ============================================================================
CREATE TABLE mold_incident (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    
    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户 ID',
    
    -- 关联区域
    zone_id               BIGINT UNSIGNED NOT NULL COMMENT '防霉管控区域 ID',
    
    -- 关联报告 (可选)
    report_id             BIGINT UNSIGNED COMMENT '关联报告 ID',
    
    -- 事件类型
    incident_type         VARCHAR(50) NOT NULL COMMENT '事件类型：HIGH_RISK_DETECTED/CONTROL_FAILED/SENSOR_OFFLINE',
    
    -- 事件等级
    severity              VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '事件等级：LOW/MEDIUM/HIGH/CRITICAL',
    
    -- 事件描述
    description           VARCHAR(1000) COMMENT '事件描述',
    
    -- 事件数据 (JSON)
    incident_data         JSON COMMENT '事件数据：{"riskLevel": "HIGH", "humidity": 85.0}',
    
    -- 处理状态
    handled               TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：1-已处理 0-未处理',
    handled_at            DATETIME(3) COMMENT '处理时间',
    handled_by            BIGINT UNSIGNED COMMENT '处理人 ID',
    
    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    created_by            BIGINT UNSIGNED COMMENT '创建人 ID',
    
    PRIMARY KEY (id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_zone_id (zone_id),
    KEY idx_report_id (report_id),
    KEY idx_incident_type (incident_type),
    KEY idx_severity (severity),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='霉菌事件表';


-- ============================================================================
-- 初始化数据
-- ============================================================================

-- 插入示例防霉管控区域
INSERT INTO mold_prevention_zone (
    tenant_id, zone_code, zone_name, space_id, zone_status,
    risk_threshold, control_strategy, current_risk_level,
    description, version, created_by
) VALUES (
    1, 'MOLD_ZONE_001', '示例防霉区域', 1, 'ACTIVE',
    '{"humidityLow": 55.0, "humidityHigh": 65.0}',
    '{"targetHumidity": 55.0, "autoAdjust": true}',
    'LOW',
    '示例防霉管控区域', 0, 1
);

SELECT '防霉管控模块数据库表创建成功！' AS status;
