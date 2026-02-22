-- ============================================================================
-- 华宽通智能体系统 - 空间管理模块数据库设计
-- 版本: V1.0
-- 数据库: MySQL 8.0+
-- 创建日期: 2026-02-20
-- ============================================================================

-- ============================================================================
-- 1. 空间表 (space)
-- 说明: 空间层级结构表，支持园区→楼栋→楼层→房间四级层级
-- ============================================================================
CREATE TABLE space (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 空间标识
    space_code            VARCHAR(100) NOT NULL COMMENT '空间编码',
    space_name            VARCHAR(200) NOT NULL COMMENT '空间名称',

    -- 空间层级
    space_type            VARCHAR(50) NOT NULL COMMENT '空间类型: PARK/BUILDING/FLOOR/ROOM',
    space_level           TINYINT NOT NULL COMMENT '空间层级: 1-园区 2-楼栋 3-楼层 4-房间',

    -- 层级关系
    parent_space_id       BIGINT UNSIGNED COMMENT '父空间ID',
    root_space_id         BIGINT UNSIGNED COMMENT '根空间ID(园区ID)',
    space_path            VARCHAR(500) COMMENT '空间路径: /园区1/楼栋A/楼层3/房间301',

    -- 位置信息
    address               VARCHAR(500) COMMENT '详细地址',
    province              VARCHAR(50) COMMENT '省份',
    city                  VARCHAR(50) COMMENT '城市',
    district              VARCHAR(50) COMMENT '区县',
    longitude             DECIMAL(10, 7) COMMENT '经度',
    latitude              DECIMAL(10, 7) COMMENT '纬度',
    altitude              DECIMAL(8, 2) COMMENT '海拔(米)',

    -- 空间边界(多边形坐标JSON)
    boundary              JSON COMMENT '空间边界: [[lng1,lat1],[lng2,lat2],...]',
    area                  DECIMAL(10, 2) COMMENT '面积(平方米)',

    -- 空间属性
    floor_number          INT COMMENT '楼层数',
    room_number           VARCHAR(50) COMMENT '房间号',
    capacity              INT COMMENT '容量(人数/设备数等)',

    -- 状态信息
    space_status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '空间状态: ACTIVE/INACTIVE/MAINTENANCE',
    usage_status          VARCHAR(20) COMMENT '使用状态: OCCUPIED/VACANT/RESERVED',

    -- 扩展属性(JSON)
    ext_properties        JSON COMMENT '扩展属性: {"key1": "value1"}',

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
    UNIQUE KEY uk_tenant_code (tenant_id, space_code, deleted),
    KEY idx_tenant_id (tenant_id),
    KEY idx_space_type (space_type),
    KEY idx_space_level (space_level),
    KEY idx_parent_space_id (parent_space_id),
    KEY idx_root_space_id (root_space_id),
    KEY idx_space_status (space_status),
    KEY idx_usage_status (usage_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='空间表';


-- ============================================================================
-- 2. 空间资源关联表 (space_resource)
-- 说明: 空间与抽象资源(设备/用户/资产等)的关联关系表
-- ============================================================================
CREATE TABLE space_resource (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 关联空间
    space_id              BIGINT UNSIGNED NOT NULL COMMENT '空间ID',
    space_code            VARCHAR(100) NOT NULL COMMENT '空间编码',

    -- 资源信息
    resource_type         VARCHAR(50) NOT NULL COMMENT '资源类型: DEVICE/USER/ASSET/EQUIPMENT',
    resource_id           BIGINT UNSIGNED NOT NULL COMMENT '资源ID',
    resource_code         VARCHAR(100) COMMENT '资源编码',

    -- 关联关系
    relation_type         VARCHAR(50) NOT NULL COMMENT '关联类型: OWNER/OCCUPANT/MANAGER/TEMPORARY',
    primary_relation      TINYINT NOT NULL DEFAULT 0 COMMENT '主关联: 1-是 0-否',

    -- 位置详情
    location_detail       VARCHAR(500) COMMENT '位置详情',
    floor_number          INT COMMENT '所在楼层',
    room_number           VARCHAR(50) COMMENT '房间号',

    -- 时间范围
    start_date            DATETIME(3) COMMENT '生效开始时间',
    end_date              DATETIME(3) COMMENT '生效结束时间',

    -- 状态
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE',

    -- 扩展属性
    ext_properties        JSON COMMENT '扩展属性',

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
    UNIQUE KEY uk_space_resource (space_id, resource_type, resource_id, deleted),
    KEY idx_tenant_id (tenant_id),
    KEY idx_space_id (space_id),
    KEY idx_resource_type_id (resource_type, resource_id),
    KEY idx_relation_type (relation_type),
    KEY idx_status (status),
    KEY idx_start_end_date (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='空间资源关联表';


-- ============================================================================
-- 3. 逻辑空间分组表 (logical_space_group)
-- 说明: 按应用/租户的空间分组
-- ============================================================================
CREATE TABLE logical_space_group (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 分组信息
    group_code            VARCHAR(100) NOT NULL COMMENT '分组编码',
    group_name            VARCHAR(200) NOT NULL COMMENT '分组名称',
    group_type            VARCHAR(50) NOT NULL COMMENT '分组类型: APPLICATION/TENANT/BUSINESS',

    -- 分组属性
    description           VARCHAR(500) COMMENT '分组描述',
    group_color           VARCHAR(20) COMMENT '分组颜色(前端展示)',
    group_icon            VARCHAR(200) COMMENT '分组图标URL',

    -- 分组规则(JSON)
    group_rule            JSON COMMENT '分组规则: {"spaceType": ["FLOOR", "ROOM"], "tags": ["tag1"]}',

    -- 状态信息
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE',
    display_order         INT DEFAULT 0 COMMENT '显示顺序',

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
    UNIQUE KEY uk_tenant_group_code (tenant_id, group_code, deleted),
    KEY idx_tenant_id (tenant_id),
    KEY idx_group_type (group_type),
    KEY idx_status (status),
    KEY idx_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='逻辑空间分组表';


-- ============================================================================
-- 4. 逻辑空间组成员表 (logical_space_group_member)
-- 说明: 逻辑空间分组成员关系表
-- ============================================================================
CREATE TABLE logical_space_group_member (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户隔离
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 关联分组
    group_id              BIGINT UNSIGNED NOT NULL COMMENT '分组ID',
    group_code            VARCHAR(100) NOT NULL COMMENT '分组编码',

    -- 关联空间
    space_id              BIGINT UNSIGNED NOT NULL COMMENT '空间ID',
    space_code            VARCHAR(100) NOT NULL COMMENT '空间编码',

    -- 成员属性
    display_order         INT DEFAULT 0 COMMENT '显示顺序',
    is_default            TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认: 1-是 0-否',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    created_by            BIGINT UNSIGNED COMMENT '创建人ID',
    updated_by            BIGINT UNSIGNED COMMENT '更新人ID',

    PRIMARY KEY (id),
    UNIQUE KEY uk_group_space (group_id, space_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_space_id (space_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='逻辑空间组成员表';


-- ============================================================================
-- 分库分表策略说明
-- ============================================================================
/*
空间管理模块分库分表策略：

1. 分库策略：按租户ID分库
   - 中小租户共享库，大租户独立库
   - 分库键：tenant_id

2. 分表策略：
   - space表：单园区数据量小，一般不需要分表
   - space_resource表：资源关联数据量大，可按 tenant_id + resource_type 分表

3. 索引优化：
   - space表：按 space_type, space_level, parent_space_id 建立索引
   - space_resource表：建立复合索引 (space_id, resource_type, resource_id)
   - 支持层级查询的 path 索引（space_path前缀查询）

4. 查询优化：
   - 使用 space_path 进行层级查询（LIKE 'path%'）
   - 使用 CTE（公用表表达式）查询子空间
   - 空间资源关联查询使用 JOIN 优化
*/


-- ============================================================================
-- 表关系说明 (ER图)
-- ============================================================================
/*
                    ┌─────────────────────┐
                    │       space         │ (空间表)
                    │─────────────────────│
                    │ id (PK)             │◄──┬── parent_space_id (FK自关联)
                    │ space_code (UK)     │   │
                    │ tenant_id           │   │
                    │ space_type          │   │
                    │ space_level         │   │
                    │ root_space_id       │   │
                    └─────────────────────┘   │
                         │                    │
                         │ space_id (FK)      │
                         ▼                    │
              ┌─────────────────────┐         │
              │  space_resource     │         │
              │─────────────────────│         │
              │ id (PK)             │         │
              │ space_id (FK)       │         │
              │ resource_type       │         │
              │ resource_id         │         │
              │ tenant_id           │         │
              └─────────────────────┘         │
                                                 │
         ┌─────────────────────┐                 │
         │ logical_space_group │                 │
         │─────────────────────│                 │
         │ id (PK)             │                 │
         │ group_code (UK)     │                 │
         │ tenant_id           │                 │
         │ group_type          │                 │
         └─────────────────────┘                 │
               │                                 │
               │ group_id (FK)                   │
               ▼                                 │
    ┌─────────────────────────────┐             │
    │ logical_space_group_member  │             │
    │─────────────────────────────│             │
    │ id (PK)                     │             │
    │ group_id (FK)               │             │
    │ space_id (FK)               │─────────────┘
    │ tenant_id                   │
    └─────────────────────────────┘
*/
