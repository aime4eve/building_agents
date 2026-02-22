# Phase 1数据库DDL设计 - DDD评审报告

**评审对象：** Phase 1核心模块数据库DDL设计
**评审人：** DDD架构专家
**评审日期：** 2026-02-20
**文档版本：** V1.0
**评审状态：** 通过

---

## 评审摘要

| 模块 | 表设计 | 索引设计 | 租户隔离 | 时序数据 | 分库分表 |
|------|--------|----------|----------|----------|----------|
| 设备管理 | ✅ | ✅ | ✅ | ✅ | ⚠️ |
| 空间管理 | ✅ | ✅ | ✅ | N/A | ⚠️ |
| 用户租户 | ✅ | ✅ | N/A | N/A | ⚠️ |
| 规则引擎 | ✅ | ✅ | ✅ | N/A | ⚠️ |

**总体评价：** 数据库设计整体符合DDD规范，表结构设计合理，租户隔离完整，时序数据采用CQRS模式正确分离。存在少量需要完善的地方，主要是分库分表策略的具体实施方案和部分索引优化建议。

---

## 详细评审结果

### 1. 表设计评审

#### ✅ 通过项

**1.1 命名规范**

| 检查项 | 标准 | 结果 |
|--------|------|------|
| 表名 | 小写+下划线 | ✅ device_thing_model |
| 字段名 | 小写+下划线 | ✅ device_sn, tenant_id |
| 主键 | BIGINT UNSIGNED AUTO_INCREMENT | ✅ id |
| 时间戳 | DATETIME(3)毫秒精度 | ✅ created_at, updated_at |

**1.2 主键设计**

- ✅ 所有表使用BIGINT UNSIGNED AUTO_INCREMENT作为主键
- ✅ 符合分布式ID生成规范
- ✅ 预留了业务ID字段（device_sn, space_code等）

**1.3 租户隔离**

- ✅ 所有业务表包含tenant_id字段
- ✅ 唯一索引包含tenant_id（如uk_tenant_sn）
- ✅ tenant_id字段建立索引

**1.4 版本字段（乐观锁）**

- ✅ 核心表包含version字段（BIGINT类型）
- ✅ 支持CAS（Compare And Set）更新

**1.5 软删除**

- ✅ 所有表支持软删除
- ✅ 包含deleted, deleted_at, deleted_by字段
- ✅ 唯一索引包含deleted字段

**1.6 审计字段**

- ✅ 标准审计字段：created_at, updated_at, created_by, updated_by
- ✅ 时间戳使用DATETIME(3)毫秒精度
- ✅ 自动更新：updated_at ON UPDATE CURRENT_TIMESTAMP(3)

#### ⚠️ 警告项

**1. 设备状态枚举值不完整**

**当前设计：**
```sql
device_status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE' COMMENT '设备状态: ONLINE/OFFLINE/DISABLED/FAULT'
```

**系统设计说明书定义：**
```
设备状态：ONLINE, OFFLINE, FAULT, MAINTENANCE, INACTIVE
```

**问题：**
1. 缺少MAINTENANCE状态
2. DISABLED应改为INACTIVE以保持一致

**建议修改：**
```sql
device_status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE' COMMENT '设备状态: ONLINE/OFFLINE/FAULT/MAINTENANCE/INACTIVE'
```

---

### 2. 索引设计评审

#### ✅ 通过项

**2.1 租户索引**

- ✅ 所有业务表包含tenant_id索引
- ✅ 支持租户级数据隔离查询

**2.2 唯一索引**

| 表 | 唯一索引 | 评价 |
|----|---------|------|
| device | uk_tenant_sn (tenant_id, device_sn, deleted) | ✅ 正确 |
| device | uk_tenant_code (tenant_id, device_code, deleted) | ✅ 正确 |
| space | uk_tenant_code (tenant_id, space_code, deleted) | ✅ 正确 |
| tenant | uk_tenant_code (tenant_code, deleted) | ✅ 正确 |
| user | uk_tenant_username (tenant_id, username, deleted) | ✅ 正确 |
| rule | uk_tenant_rule_code (tenant_id, rule_code, deleted) | ✅ 正确 |

**2.3 外键索引**

- ✅ 所有外键字段建立索引（space_id, thing_model_id等）
- ✅ 支持关联查询优化

**2.4 状态索引**

- ✅ device_status, online_status, space_status等状态字段建立索引
- ✅ 支持状态过滤查询

**2.5 时间索引**

- ✅ last_online_time, created_at, triggered_at等时间字段建立索引
- ✅ 支持时间范围查询

#### ⚠️ 警告项

**2.1 缺少复合索引优化**

**问题场景：** 按租户+状态+在线状态查询设备

**当前索引：**
```sql
KEY idx_tenant_id (tenant_id),
KEY idx_device_status (device_status),
KEY idx_online_status (online_status)
```

**建议增加复合索引：**
```sql
KEY idx_tenant_status_online (tenant_id, device_status, online_status)
```

**2.2 缺少设备License过期提醒索引**

**当前设计：**
```sql
KEY idx_end_date (end_date)
```

**建议增加复合索引：**
```sql
KEY idx_tenant_status_end_date (tenant_id, license_status, end_date)
```

**2.3 规则执行日志缺少状态+时间复合索引**

**当前索引：**
```sql
KEY idx_execution_status (execution_status),
KEY idx_triggered_at (triggered_at)
```

**建议增加复合索引：**
```sql
KEY idx_status_triggered_at (execution_status, triggered_at)
```

---

### 3. 时序数据设计评审

#### ✅ 通过项

**3.1 CQRS模式实现**

- ✅ 写侧快照：device_telemetry_snapshot（存储最新状态）
- ✅ 读侧时序：通过注释说明历史数据存InfluxDB/TDengine
- ✅ 符合系统设计说明书的"写侧快照+读侧时序"分离策略

**3.2 快照表设计**

```sql
CREATE TABLE device_telemetry_snapshot (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    device_id BIGINT UNSIGNED NOT NULL,
    telemetry_data JSON NOT NULL,
    event_time DATETIME(3) NOT NULL,
    UNIQUE KEY uk_device_snapshot (device_id, tenant_id),
    ...
)
```

**评价：**
- ✅ 使用UNIQUE KEY保证每设备一条快照
- ✅ 支持UPSERT操作
- ✅ JSON格式存储灵活的遥测数据

**3.3 时序数据保留策略**

- ✅ 注释中说明历史数据存时序数据库
- ✅ 注释中说明保留策略（7天/30天/90天/1年）

#### ⚠️ 警告项

**3.1 缺少时序数据库DDL**

**问题：** 注释说明历史数据存InfluxDB/TDengine，但没有提供对应的DDL

**建议：** 补充时序数据库DDL文件
```
/database/ddl/timeseries/
├── influxdb/
│   └── device_telemetry_history.flux
└── tdengine/
    └── device_telemetry_history.sql
```

---

### 4. 分库分表策略评审

#### ✅ 通过项

**4.1 分库策略设计**

- ✅ 按租户ID分库
- ✅ 中小租户共享库，大租户独立库
- ✅ 分库键：tenant_id

**4.2 分表策略设计**

- ✅ device表：按设备数量分表（>500万）
- ✅ rule_execution_log表：按时间分区
- ✅ sso_session表：按时间分区

**4.3 分表算法**

- ✅ hash(device_id) % 表数量
- ✅ 按时间范围分区

#### ⚠️ 警告项

**4.1 缺少具体实施方案**

**问题：** 注释中说明了分库分表策略，但没有提供：
1. 分库分表路由规则配置
2. 数据迁移脚本
3. 跨分片JOIN解决方案

**建议：** 补充以下文件
```
/database/
├── sharding/
│   ├── sharding-rule.yaml      # 分库分表规则配置
│   ├── data-migration.sql      # 数据迁移脚本
│   └── cross-shard-query.md    # 跨分片查询方案
```

**4.2 缺少分区表DDL示例**

**问题：** rule_execution_log表说明需要按时间分区，但DDL中未体现

**建议：** 补充分区表DDL
```sql
CREATE TABLE rule_execution_log (
    ...
    PRIMARY KEY (id, triggered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
PARTITION BY RANGE (TO_DAYS(triggered_at)) (
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);
```

---

### 5. 数据模型与领域模型对应关系评审

#### ✅ 通过项

**5.1 聚合根对应表**

| 领域聚合根 | 数据库表 | 一致性 |
|-----------|---------|--------|
| Device | device | ✅ |
| DeviceLicense | device_license | ✅ |
| Space | space | ✅ |
| Tenant | tenant | ✅ |
| User | user | ✅ |
| Rule | rule | ✅ |

**5.2 值对象对应字段**

| 领域值对象 | 数据库字段 | 一致性 |
|-----------|-----------|--------|
| DeviceSn | device_sn | ✅ |
| DeviceType | device_type | ✅ |
| TenantId | tenant_id | ✅ |
| SpaceId | space_id | ✅ |

#### ⚠️ 警告项

**5.1 JSON字段与领域对象映射**

**问题：** 大量使用JSON字段（properties_def, services_def, rule_config等）

**影响：**
1. 领域对象转换复杂
2. JSON字段无法建立有效索引
3. 数据完整性约束无法在数据库层实现

**建议：**
1. 核心字段独立为普通字段
2. 扩展字段使用JSON
3. 提供JSON字段与领域对象的转换工具类

**示例：**
```sql
-- 当前设计
properties_def JSON COMMENT '属性定义'

-- 建议核心字段独立
property_count INT COMMENT '属性数量',
properties_def JSON COMMENT '扩展属性定义',
```

---

## 与系统设计说明书的对应关系

### 限界上下文映射

| DDL文件 | 系统设计说明书限界上下文 | 一致性 |
|---------|------------------------|--------|
| 01_device_management.sql | 设备管理（通用领域） | ✅ |
| 02_space_management.sql | 空间管理（通用领域） | ✅ |
| 03_user_tenant.sql | 用户与租户管理（通用领域） | ✅ |
| 04_rule_engine.sql | 规则引擎（通用领域） | ✅ |

### 领域模型映射

| 领域聚合根 | DDL表 | 字段一致性 | 备注 |
|-----------|-------|-----------|------|
| Device | device | ✅ | 缺少MAINTENANCE状态 |
| DeviceTelemetry | device_telemetry_snapshot | ✅ | 快照表设计正确 |
| DeviceLicense | device_license | ✅ | |
| Space | space | ✅ | |
| Tenant | tenant | ✅ | |
| User | user | ✅ | |
| Rule | rule | ✅ | |

---

## 改进建议

### 优先级：高

| 序号 | 改进项 | 负责人 | 截止日期 |
|------|--------|--------|----------|
| 1 | 修正device_status枚举值，补充MAINTENANCE状态 | database-expert | 2026-02-21 |
| 2 | 补充分区表DDL示例 | database-expert | 2026-02-21 |

### 优先级：中

| 序号 | 改进项 | 负责人 | 截止日期 |
|------|--------|--------|----------|
| 3 | 增加复合索引优化 | database-expert | 2026-02-22 |
| 4 | 补充时序数据库DDL | database-expert | 2026-02-22 |
| 5 | 补充分库分表实施方案 | database-expert | 2026-02-22 |

### 优先级：低

| 序号 | 改进项 | 负责人 | 截止日期 |
|------|--------|--------|----------|
| 6 | 补充JSON字段转换工具类说明 | database-expert | 2026-02-23 |

---

## 附录：建议的DDL修改

### 1. 修正设备状态枚举

```sql
-- 原始设计
device_status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE' COMMENT '设备状态: ONLINE/OFFLINE/DISABLED/FAULT'

-- 修改为
device_status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE' COMMENT '设备状态: ONLINE/OFFLINE/FAULT/MAINTENANCE/INACTIVE'

-- 新增维护模式索引
KEY idx_device_status (device_status)
```

### 2. 增加复合索引

```sql
-- 设备表复合索引
ALTER TABLE device
ADD KEY idx_tenant_status_online (tenant_id, device_status, online_status);

-- License表复合索引
ALTER TABLE device_license
ADD KEY idx_tenant_status_end_date (tenant_id, license_status, end_date);

-- 规则执行日志复合索引
ALTER TABLE rule_execution_log
ADD KEY idx_status_triggered_at (execution_status, triggered_at);
```

### 3. 分区表示例

```sql
-- 规则执行日志分区表
CREATE TABLE rule_execution_log (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    tenant_id BIGINT UNSIGNED NOT NULL,
    rule_id BIGINT UNSIGNED NOT NULL,
    execution_id VARCHAR(100) NOT NULL,
    execution_status VARCHAR(20) NOT NULL,
    triggered_at DATETIME(3) NOT NULL,
    -- 其他字段...
    PRIMARY KEY (id, triggered_at),
    UNIQUE KEY uk_execution_id (execution_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_rule_id (rule_id),
    KEY idx_status_triggered_at (execution_status, triggered_at)
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

-- 分区管理脚本
-- 添加新分区
ALTER TABLE rule_execution_log ADD PARTITION (
    PARTITION p202607 VALUES LESS THAN (TO_DAYS('2026-08-01'))
);

-- 删除旧分区
ALTER TABLE rule_execution_log DROP PARTITION p202601;
```

### 4. 时序数据库DDL（InfluxDB示例）

```flux
// device_telemetry_history.flux
// 创建时序数据库

// 1. 创建数据库
CREATE DATABASE "hkt_iot" WITH DURATION 90d REPLICATION 1 NAME "hkt_90d"

// 2. 创建保留策略
CREATE RETENTION POLICY "7_days" ON "hkt_iot" DURATION 7d REPLICATION 1 DEFAULT
CREATE RETENTION POLICY "30_days" ON "hkt_iot" DURATION 30d REPLICATION 1
CREATE RETENTION POLICY "90_days" ON "hkt_iot" DURATION 90d REPLICATION 1

// 3. 创建连续查询（数据聚合）
CREATE CONTINUOUS QUERY "cq_telemetry_hourly" ON "hkt_iot"
BEGIN
    SELECT mean(*) INTO "hkt_iot"."30_days".:MEASUREMENT FROM "hkt_iot"."7_days"./.*/ GROUP BY time(1h), *
END

CREATE CONTINUOUS QUERY "cq_telemetry_daily" ON "hkt_iot"
BEGIN
    SELECT mean(*) INTO "hkt_iot"."90_days".:MEASUREMENT FROM "hkt_iot"."30_days"./.*/ GROUP BY time(1d), *
END
```

---

## 评审结论

**评审结果：** 通过

**理由：**
1. 表设计符合DDD规范，命名规范统一
2. 租户隔离设计完整，tenant_id索引齐全
3. 时序数据采用CQRS模式正确分离
4. 版本字段和软删除设计规范
5. 分库分表策略设计合理
6. 存在少量需要完善的地方，主要是设备状态枚举值和索引优化

**下一步行动：**
1. database-expert根据评审意见调整DDL
2. 补充分区表DDL示例
3. 补充时序数据库DDL
4. DDD架构专家复审调整后的设计

---

**评审人签字：** DDD架构专家
**日期：** 2026-02-20
