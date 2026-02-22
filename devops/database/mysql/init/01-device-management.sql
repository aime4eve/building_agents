-- ============================================================================
-- 华宽通智能体系统 - 设备管理模块数据库初始化
-- 版本: V1.0
-- 创建日期: 2026-02-20
-- 说明: 设备管理模块表结构
-- ============================================================================

USE `hkt_iot_device`;

-- ============================================================================
-- 1. 物模型表 (device_thing_model)
-- 说明: 设备物模型定义，描述设备的属性、服务、事件
-- ============================================================================
CREATE TABLE IF NOT EXISTS device_thing_model (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 基本信息
    device_model          VARCHAR(100) NOT NULL COMMENT '设备型号',
    model_name            VARCHAR(200) NOT NULL COMMENT '物模型名称',
    category              VARCHAR(50) NOT NULL COMMENT '设备分类(传感器/控制器/网关等)',
    manufacturer          VARCHAR(100) COMMENT '厂商',

    -- 物模型定义(JSON格式)
    properties_def        JSON COMMENT '属性定义: [{name,identifier,dataType,unit,min,max,spec}]',
    services_def          JSON COMMENT '服务定义: [{name,identifier,inputArgs,outputArgs,callType}]',
    events_def            JSON COMMENT '事件定义: [{name,identifier,eventType,outputArgs,desc}]',

    -- 状态信息
    status                TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    version               BIGINT NOT NULL DEFAULT 0 COMMENT '版本号(乐观锁)',

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
    UNIQUE KEY uk_tenant_model (tenant_id, device_model, deleted),
    KEY idx_category (category),
    KEY idx_status (status),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备物模型表';


-- ============================================================================
-- 2. 设备主表 (device)
-- 说明: 设备数字孪生主表，存储设备的核心信息和最新状态快照
-- ============================================================================
CREATE TABLE IF NOT EXISTS device (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 设备标识
    device_sn             VARCHAR(100) NOT NULL COMMENT '设备序列号',
    device_name           VARCHAR(200) NOT NULL COMMENT '设备名称',
    device_code           VARCHAR(100) COMMENT '设备编码',

    -- 设备分类
    device_type           VARCHAR(50) NOT NULL COMMENT '设备类型',
    device_model          VARCHAR(100) NOT NULL COMMENT '设备型号',
    device_category       VARCHAR(50) NOT NULL COMMENT '设备分类(传感器/控制器/网关等)',

    -- 关联信息
    thing_model_id        BIGINT UNSIGNED COMMENT '物模型ID',
    space_id              BIGINT UNSIGNED COMMENT '所属空间ID',
    parent_device_id      BIGINT UNSIGNED COMMENT '父设备ID(网关子设备关系)',

    -- 设备状态
    device_status         VARCHAR(20) NOT NULL DEFAULT 'INACTIVE' COMMENT '设备状态: ONLINE/OFFLINE/FAULT/MAINTENANCE/INACTIVE',
    online_status         TINYINT NOT NULL DEFAULT 0 COMMENT '在线状态: 1-在线 0-离线',
    activation_status     VARCHAR(20) NOT NULL DEFAULT 'INACTIVE' COMMENT '激活状态: ACTIVE/INACTIVE',

    -- 位置信息
    location_desc         VARCHAR(500) COMMENT '位置描述',
    longitude             DECIMAL(10, 7) COMMENT '经度',
    latitude              DECIMAL(10, 7) COMMENT '纬度',
    altitude              DECIMAL(8, 2) COMMENT '海拔(米)',

    -- 网络信息
    ip_address            VARCHAR(50) COMMENT 'IP地址',
    mac_address           VARCHAR(50) COMMENT 'MAC地址',
    gateway_id            BIGINT UNSIGNED COMMENT '所属网关ID',

    -- 固件信息
    firmware_version      VARCHAR(50) COMMENT '固件版本',
    hardware_version      VARCHAR(50) COMMENT '硬件版本',
    software_version      VARCHAR(50) COMMENT '软件版本',

    -- 最新状态快照(JSON格式，写侧快照)
    latest_properties     JSON COMMENT '最新属性值快照',
    latest_event          JSON COMMENT '最新事件快照',
    last_online_time      DATETIME(3) COMMENT '最后上线时间',
    last_offline_time     DATETIME(3) COMMENT '最后离线时间',
    last_data_time        DATETIME(3) COMMENT '最后数据上报时间',

    -- 控制信息
    is_locked             TINYINT NOT NULL DEFAULT 0 COMMENT '锁定状态: 1-锁定 0-正常',
    locked_by             BIGINT UNSIGNED COMMENT '锁定人ID',
    locked_at             DATETIME(3) COMMENT '锁定时间',
    lock_reason           VARCHAR(500) COMMENT '锁定原因',

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
    UNIQUE KEY uk_tenant_sn (tenant_id, device_sn, deleted),
    UNIQUE KEY uk_tenant_code (tenant_id, device_code, deleted),
    KEY idx_tenant_id (tenant_id),
    KEY idx_device_type (device_type),
    KEY idx_device_status (device_status),
    KEY idx_online_status (online_status),
    KEY idx_tenant_status_online (tenant_id, device_status, online_status),
    KEY idx_tenant_online_status (tenant_id, online_status),
    KEY idx_space_id (space_id),
    KEY idx_parent_device_id (parent_device_id),
    KEY idx_thing_model_id (thing_model_id),
    KEY idx_last_online_time (last_online_time),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备主表';


-- ============================================================================
-- 3. 设备License表 (device_license)
-- 说明: 设备授权管控表
-- ============================================================================
CREATE TABLE IF NOT EXISTS device_license (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 关联设备
    device_id             BIGINT UNSIGNED NOT NULL COMMENT '设备ID',
    device_sn             VARCHAR(100) NOT NULL COMMENT '设备序列号',

    -- License信息
    license_key           VARCHAR(200) NOT NULL COMMENT 'License密钥',
    license_type          VARCHAR(50) NOT NULL COMMENT 'License类型: STANDARD/PREMIUM/ENTERPRISE',
    license_status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'License状态: ACTIVE/SUSPENDED/EXPIRED/REVOKED',

    -- 有效期
    start_date            DATE NOT NULL COMMENT '生效日期',
    end_date              DATE COMMENT '到期日期(NULL表示永久)',

    -- 功能授权
    authorized_features   JSON COMMENT '授权功能列表: ["feature1", "feature2"]',
    max_devices           INT COMMENT '最大子设备数量限制',

    -- 计费信息
    billing_cycle         VARCHAR(20) COMMENT '计费周期: MONTHLY/YEARLY',
    fee_amount            DECIMAL(10, 2) COMMENT '费用金额',
    currency              VARCHAR(10) DEFAULT 'CNY' COMMENT '货币类型',

    -- 续费信息
    auto_renew            TINYINT NOT NULL DEFAULT 0 COMMENT '自动续费: 1-是 0-否',
    last_renew_date       DATE COMMENT '最后续费日期',
    next_billing_date     DATE COMMENT '下次计费日期',

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
    UNIQUE KEY uk_license_key (license_key, deleted),
    UNIQUE KEY uk_device_license (device_id, deleted),
    KEY idx_tenant_id (tenant_id),
    KEY idx_device_sn (device_sn),
    KEY idx_license_status (license_status),
    KEY idx_end_date (end_date),
    KEY idx_tenant_status_end_date (tenant_id, license_status, end_date),
    KEY idx_tenant_status (tenant_id, license_status),
    KEY idx_next_billing_date (next_billing_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备License表';


-- ============================================================================
-- 4. 设备快照表-写侧 (device_telemetry_snapshot)
-- 说明: 设备遥测数据快照表，存储设备最新状态(写侧快照)
-- 注意: 历史时序数据存储在时序数据库(InfluxDB/TDengine)
-- ============================================================================
CREATE TABLE IF NOT EXISTS device_telemetry_snapshot (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 关联设备
    device_id             BIGINT UNSIGNED NOT NULL COMMENT '设备ID',
    device_sn             VARCHAR(100) NOT NULL COMMENT '设备序列号',

    -- 遥测数据(JSON格式)
    telemetry_data        JSON NOT NULL COMMENT '遥测数据: {"temp": 25.5, "humidity": 60, ...}',

    -- 数据质量
    data_quality          TINYINT NOT NULL DEFAULT 1 COMMENT '数据质量: 1-好 2-一般 3-差',
    data_source           VARCHAR(50) DEFAULT 'DEVICE' COMMENT '数据来源: DEVICE/EDGE/CALCULATED',

    -- 时间戳
    event_time            DATETIME(3) NOT NULL COMMENT '事件时间(设备上报时间)',
    received_time         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '接收时间',

    -- 统计信息
    report_count          BIGINT NOT NULL DEFAULT 1 COMMENT '上报次数',

    -- 版本控制(乐观锁)
    version               BIGINT NOT NULL DEFAULT 0 COMMENT '版本号',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_device_snapshot (device_id, tenant_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_device_sn (device_sn),
    KEY idx_event_time (event_time),
    KEY idx_received_time (received_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备遥测数据快照表';

-- 显示创建的表
SHOW TABLES;
SELECT 'Device management module tables created successfully!' AS status;
