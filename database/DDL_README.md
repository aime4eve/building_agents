# 华宽通智能体系统 - Phase 1 数据库设计文档

## 版本信息
- 版本: V1.1
- 创建日期: 2026-02-20
- 更新日期: 2026-02-20
- 数据库: MySQL 8.0+

## 变更记录

| 版本 | 日期 | 变更内容 | 变更人 |
|------|------|----------|--------|
| V1.0 | 2026-02-20 | 初始版本 | database-expert |
| V1.1 | 2026-02-20 | 1.修正device_status枚举值（增加MAINTENANCE）<br>2.增加复合索引优化<br>3.添加分区表DDL<br>4.添加分区管理脚本 | database-expert |

## 目录
1. [设计原则](#设计原则)
2. [模块概览](#模块概览)
3. [分库分表策略](#分库分表策略)
4. [索引策略](#索引策略)
5. [ER关系图](#er关系图)

---

## 设计原则

### 1. 通用字段规范
所有表包含以下字段：
- `id`: BIGINT UNSIGNED 主键，自增
- `tenant_id`: BIGINT UNSIGNED 租户ID（数据隔离）
- `created_at`: DATETIME(3) 创建时间
- `updated_at`: DATETIME(3) 更新时间
- `version`: BIGINT 版本号（乐观锁）
- `deleted`: TINYINT 删除标记（软删除）

### 2. 命名规范
- 表名：小写，下划线分隔，如 `device_telemetry_snapshot`
- 索引名：`idx_` 前缀 + 字段名
- 唯一索引：`uk_` 前缀 + 字段名
- 外键：`<column>_id` 后缀

### 3. 数据类型规范
- 主键/外键：`BIGINT UNSIGNED`
- 金额：`DECIMAL(10, 2)`
- 状态：`VARCHAR(20)` 或 `TINYINT`
- 时间：`DATETIME(3)`（毫秒精度）
- JSON数据：`JSON` 类型

---

## 模块概览

### 1. 设备管理模块 (01_device_management.sql)
| 表名 | 说明 | 记录数预估 |
|------|------|------------|
| device_thing_model | 物模型定义 | < 1000 |
| device | 设备主表 | 10,000+ |
| device_license | 设备License | 10,000+ |
| device_telemetry_snapshot | 设备快照(写侧) | 10,000+ |

### 2. 空间管理模块 (02_space_management.sql)
| 表名 | 说明 | 记录数预估 |
|------|------|------------|
| space | 空间层级表 | < 50,000 |
| space_resource | 空间资源关联 | 100,000+ |
| logical_space_group | 逻辑空间分组 | < 5,000 |
| logical_space_group_member | 分组成员 | 50,000+ |

### 3. 用户与租户模块 (03_user_tenant.sql)
| 表名 | 说明 | 记录数预估 |
|------|------|------------|
| tenant | 租户表 | < 10,000 |
| user | 用户表 | 100,000+ |
| role | 角色表 | < 50,000 |
| permission | 权限表 | < 1,000 |
| user_role | 用户角色关联 | 500,000+ |
| role_permission | 角色权限关联 | 1,000,000+ |
| sso_session | SSO会话表 | 1,000,000+ |
| mfa_config | MFA配置表 | 100,000+ |
| mfa_device | MFA设备表 | 100,000+ |
| mfa_challenge | MFA挑战表 | 10,000,000+ |

### 4. 规则引擎模块 (04_rule_engine.sql)
| 表名 | 说明 | 记录数预估 |
|------|------|------------|
| rule | 规则表 | < 100,000 |
| rule_condition | 规则条件表 | 500,000+ |
| rule_action | 规则动作表 | 500,000+ |
| rule_execution_log | 规则执行日志 | 100,000,000+ |
| rule_action_execution_log | 动作执行日志 | 500,000,000+ |
| rule_variable | 规则变量表 | 100,000+ |

---

## 分库分表策略

### 1. 分库策略
```
┌─────────────────────────────────────────────────────┐
│                   应用层                             │
└─────────────────────────────────────────────────────┘
                        │
         ┌──────────────┼──────────────┐
         ▼              ▼              ▼
  ┌──────────┐  ┌──────────┐  ┌──────────┐
  │ 大租户库  │  │ 中租户库  │  │ 小租户库  │
  │  独立DB  │  │  共享DB  │  │  共享DB  │
  └──────────┘  └──────────┘  └──────────┘
```

**分库规则：**
- 大租户（设备数>10万）：独立数据库
- 中租户（设备数1万-10万）：共享数据库（按租户ID范围分库）
- 小租户（设备数<1万）：共享数据库

### 2. 分表策略

**设备表 (device)：**
```sql
-- 按 device_id 哈希分表
分表数量：16
分表键：device_id
算法：HASH(device_id) % 16

-- 分表命名：device_0, device_1, ..., device_15
```

**用户表 (user)：**
```sql
-- 按 tenant_id + user_id 分表
分表数量：按租户规模动态调整
大租户：独立表
中小租户：共享表
```

**规则执行日志 (rule_execution_log)：**
```sql
-- 按时间分区
PARTITION BY RANGE (TO_DAYS(triggered_at))
(
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    ...
)

-- 保留策略：在线90天，历史归档
```

### 3. 时序数据策略
```
设备遥测数据存储策略：
├── 写侧（MySQL）
│   └── device_telemetry_snapshot (最新快照，每设备1条)
└── 读侧（InfluxDB/TDengine）
    └── 历史时序数据
        ├── 热数据：7天
        ├── 温数据：30天
        └── 冷数据：90天+
```

---

## 索引策略

### 1. 租户隔离索引
所有表必须包含：
```sql
KEY idx_tenant_id (tenant_id)
```

### 2. 复合索引策略
```sql
-- 设备表
KEY idx_tenant_status (tenant_id, device_status)
KEY idx_tenant_type_status (tenant_id, device_type, online_status)

-- 用户表
UNIQUE KEY uk_tenant_username (tenant_id, username, deleted)
KEY idx_tenant_status (tenant_id, user_status)

-- 规则表
KEY idx_tenant_status_enabled (tenant_id, rule_status, is_enabled)
```

### 3. 时间范围查询索引
```sql
-- 执行日志表
KEY idx_triggered_at (triggered_at)
KEY idx_tenant_triggered (tenant_id, triggered_at)

-- 会话表
KEY idx_expires_at (expires_at)
```

---

## ER关系图

### 核心实体关系
```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Phase 1 模块关系图                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐              │
│  │  tenant  │───▶│   user   │───▶│   role   │───▶│permission│              │
│  │  (租户)   │    │  (用户)   │    │  (角色)   │    │ (权限)   │              │
│  └──────────┘    └──────────┘    └──────────┘    └──────────┘              │
│       │                │                │                                  │
│       │                │                │                                  │
│       ▼                ▼                ▼                                  │
│  ┌──────────────────────────────────────────────────────────────┐          │
│  │                         space                                │          │
│  │                        (空间)                                 │          │
│  │  ┌──────────┐  ┌──────────────────┐  ┌──────────────────┐    │          │
│  │  │  space   │  │ space_resource   │  │ logical_space_   │    │          │
│  │  │  (层级)   │  │   (资源关联)      │  │      group       │    │          │
│  │  └──────────┘  └──────────────────┘  └──────────────────┘    │          │
│  └──────────────────────────────────────────────────────────────┘          │
│       │                                                                  │
│       │                                                                  │
│       ▼                                                                  │
│  ┌──────────────────────────────────────────────────────────────┐          │
│  │                         device                                │          │
│  │                        (设备)                                 │          │
│  │  ┌──────────────┐  ┌──────────────────┐  ┌──────────────┐    │          │
│  │  │   device     │  │device_thing_model│  │device_license│    │          │
│  │  │   (主表)     │  │   (物模型)        │  │  (License)   │    │          │
│  │  └──────────────┘  └──────────────────┘  └──────────────┘    │          │
│  │                                                                  │          │
│  │  ┌────────────────────────────────────────────────────────┐   │          │
│  │  │          device_telemetry_snapshot (快照-写侧)           │   │          │
│  │  └────────────────────────────────────────────────────────┘   │          │
│  └──────────────────────────────────────────────────────────────┘          │
│       │                                                                  │
│       │                                                                  │
│       ▼                                                                  │
│  ┌──────────────────────────────────────────────────────────────┐          │
│  │                         rule                                  │          │
│  │                        (规则)                                 │          │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │          │
│  │  │    rule      │  │rule_condition│  │  rule_action │        │          │
│  │  │   (规则)     │  │  (规则条件)   │  │  (规则动作)   │        │          │
│  │  └──────────────┘  └──────────────┘  └──────────────┘        │          │
│  │                                                                  │          │
│  │  ┌────────────────────────────────────────────────────────┐   │          │
│  │  │              rule_execution_log (执行日志)               │   │          │
│  │  └────────────────────────────────────────────────────────┘   │          │
│  └──────────────────────────────────────────────────────────────┘          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 使用说明

### 1. DDL执行顺序
```bash
# 1. 创建数据库
CREATE DATABASE huakuantong_agent CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 按模块顺序执行DDL
mysql -u root -p huakuantong_agent < 01_device_management.sql
mysql -u root -p huakuantong_agent < 02_space_management.sql
mysql -u root -p huakuantong_agent < 03_user_tenant.sql
mysql -u root -p huakuantong_agent < 04_rule_engine.sql
```

### 2. 初始化数据
需要初始化的数据：
1. 系统默认租户
2. 超级管理员账号
3. 系统默认角色
4. 系统默认权限

### 3. 时序数据库配置
InfluxDB/TDengine 配置：
```sql
-- InfluxDB
CREATE DATABASE huakuantong_telemetry
RETENTION 90d
DURATION 7d
REPLICATION 1
SHARD DURATION 1d;

-- TDengine
CREATE DATABASE IF NOT EXISTS telemetry KEEP 90;
```

---

## 附录：表清单

| 序号 | 模块 | 表名 | 说明 |
|------|------|------|------|
| 1 | 设备管理 | device_thing_model | 物模型定义 |
| 2 | 设备管理 | device | 设备主表 |
| 3 | 设备管理 | device_license | 设备License |
| 4 | 设备管理 | device_telemetry_snapshot | 设备快照 |
| 5 | 空间管理 | space | 空间表 |
| 6 | 空间管理 | space_resource | 空间资源关联 |
| 7 | 空间管理 | logical_space_group | 逻辑空间分组 |
| 8 | 空间管理 | logical_space_group_member | 分组成员 |
| 9 | 用户租户 | tenant | 租户表 |
| 10 | 用户租户 | user | 用户表 |
| 11 | 用户租户 | role | 角色表 |
| 12 | 用户租户 | permission | 权限表 |
| 13 | 用户租户 | user_role | 用户角色关联 |
| 14 | 用户租户 | role_permission | 角色权限关联 |
| 15 | 用户租户 | sso_session | SSO会话表 |
| 16 | 用户租户 | mfa_config | MFA配置 |
| 17 | 用户租户 | mfa_device | MFA设备 |
| 18 | 用户租户 | mfa_challenge | MFA挑战 |
| 19 | 规则引擎 | rule | 规则表 |
| 20 | 规则引擎 | rule_condition | 规则条件 |
| 21 | 规则引擎 | rule_action | 规则动作 |
| 22 | 规则引擎 | rule_execution_log | 执行日志 |
| 23 | 规则引擎 | rule_action_execution_log | 动作执行日志 |
| 24 | 规则引擎 | rule_variable | 规则变量 |

---

**文档结束**
