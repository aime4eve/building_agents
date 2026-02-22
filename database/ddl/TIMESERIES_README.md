# 时序数据库设计说明文档

## 版本信息
- 版本: V1.0
- 创建日期: 2026-02-20
- 支持数据库: InfluxDB 2.x / TDengine 3.x

---

## 一、设计概述

时序数据库用于存储设备遥测数据和事件数据，与MySQL关系型数据库形成**读写分离架构**：

| 存储位置 | 数据类型 | 保留时间 | 用途 |
|----------|----------|----------|------|
| MySQL | 设备快照 | 最新1条 | 当前状态查询、事务处理 |
| InfluxDB/TDengine | 历史时序数据 | 7-90天 | 趋势分析、统计聚合 |

---

## 二、InfluxDB 2.x 设计

### 2.1 数据组织结构

```
Bucket: huakuantong_telemetry
├── device_telemetry     (设备遥测数据)
├── device_event         (设备事件数据)
└── device_command       (设备命令记录)
```

### 2.2 数据模型

#### device_telemetry (设备遥测数据)

| 类型 | 字段 | 说明 |
|------|------|------|
| Tag | tenant_id | 租户ID |
| Tag | device_id | 设备ID |
| Tag | device_sn | 设备序列号 |
| Tag | device_type | 设备类型 |
| Tag | device_model | 设备型号 |
| Tag | space_id | 空间ID |
| Tag | data_source | 数据来源 |
| Field | temperature/humidity/etc. | 动态属性值 |
| Field | data_quality | 数据质量 |

#### device_event (设备事件数据)

| 类型 | 字段 | 说明 |
|------|------|------|
| Tag | tenant_id | 租户ID |
| Tag | device_id | 设备ID |
| Tag | event_type | 事件类型 |
| Tag | event_level | 事件级别 |
| Tag | space_id | 空间ID |
| Field | event_message | 事件消息 |
| Field | event_code | 事件代码 |
| Field | event_value | 事件值 |
| Field | duration | 持续时间 |

### 2.3 数据保留策略

| 策略名 | 保留时间 | 分片时长 | 说明 |
|--------|----------|----------|------|
| hot_rp | 7 days | 1 day | 热数据，高频查询 |
| warm_rp | 30 days | 7 days | 温数据，降采样 |
| cold_rp | 365 days | 30 days | 冷数据，归档查询 |

---

## 三、TDengine 3.x 设计

### 3.1 数据库结构

```
Database: telemetry (KEEP 90)
├── device_telemetry_s    (超级表：设备遥测)
├── device_event_s        (超级表：设备事件)
└── device_command_s      (超级表：设备命令)
```

### 3.2 超级表定义

#### device_telemetry_s

```sql
CREATE STABLE device_telemetry_s (
  ts TIMESTAMP,                    -- 时间戳
  data_quality INT,                -- 数据质量
  metric_name NCHAR(100),          -- 指标名称
  metric_value DOUBLE,             -- 指标值
  metric_str NCHAR(500)            -- 字符串值
) TAGS (
  tenant_id NCHAR(50),             -- 租户ID
  device_id NCHAR(50),             -- 设备ID
  device_sn NCHAR(100),            -- 设备序列号
  device_type NCHAR(50),           -- 设备类型
  space_id NCHAR(50)               -- 空间ID
);
```

### 3.3 子表自动创建

TDengine 支持自动创建子表：

```sql
-- 插入数据时自动创建子表
INSERT INTO device_telemetry_1001 USING device_telemetry_s
TAGS ('1', '1001', 'SN001', 'sensor', '101')
VALUES (NOW, 1, 'temperature', 25.5, NULL);
```

---

## 四、读写分离架构

### 4.1 数据流向

```
┌─────────────┐
│ 设备数据上报 │
└──────┬──────┘
       │
   ┌───┴────┬─────────┐
   ▼        ▼         ▼
┌─────┐ ┌────┐ ┌─────────┐
│MySQL│ │Influx│ │TDengine │
│快照 │ │历史 │ │历史     │
└─────┘ └────┘ └─────────┘
   │        │         │
   └────────┴─────────┘
            │
    ┌───────┴───────┐
    │  应用层查询   │
    └───────────────┘
```

### 4.2 写入流程

1. **同步写入 MySQL**
   - 更新 `device_telemetry_snapshot` 表
   - 每设备保留1条最新快照
   - 保证 ACID 一致性

2. **异步写入时序数据库**
   - 通过 Kafka 消息队列
   - 批量写入提高性能
   - 保留完整历史数据

### 4.3 查询路由

| 查询类型 | 存储位置 | 示例 |
|----------|----------|------|
| 设备当前状态 | MySQL | SELECT * FROM device_telemetry_snapshot |
| 历史趋势 | Influx/TDengine | WHERE time > now() - 24h |
| 统计聚合 | Influx/TDengine | GROUP BY device_id, time(1h) |
| 事件统计 | Influx/TDengine | WHERE event_type = 'FAULT' |

---

## 五、性能优化

### 5.1 写入优化

1. **批量写入**
   - InfluxDB: 每批 5000-10000 点
   - TDengine: 每批 1000-5000 行

2. **异步写入**
   - 使用 Kafka 缓冲
   - 批量提交

### 5.2 查询优化

1. **限制时间范围**
   - 必须指定时间范围
   - 避免全表扫描

2. **使用聚合函数**
   - InfluxDB: `aggregateWindow()`
   - TDengine: `INTERVAL()`

3. **合理使用 Tag 过滤**
   - Tag 是索引字段
   - 优先用 Tag 过滤

---

## 六、数据迁移策略

### 6.1 冷热数据分离

```
热数据 (7天)  → 温数据 (30天) → 冷数据 (365天) → 归档/删除
   高精度         5分钟降采样      1小时降采样
```

### 6.2 降采样任务

```sql
-- InfluxDB Task
from(bucket: "hot_rp")
  |> range(start: -1h)
  |> aggregateWindow(every: 5m, fn: mean)
  |> to(bucket: "warm_rp")

-- TDengine Stream
CREATE STREAM stream_to_warm AS
SELECT _wstart, device_id, AVG(metric_value)
FROM hot.device_telemetry_s
INTERVAL(5m)
INTO warm.device_telemetry_s;
```

---

## 七、监控与告警

### 7.1 监控指标

| 指标 | 说明 | 告警阈值 |
|------|------|----------|
| 写入速率 | 每秒写入点数 | < 10000 points/s |
| 查询延迟 | 查询响应时间 | > 5s |
| 存储空间 | 磁盘使用率 | > 80% |

### 7.2 数据质量监控

- 检查数据完整性
- 监控 `data_quality` 字段
- 告警异常数据

---

## 八、故障恢复

### 8.1 备份策略

| 备份类型 | 频率 | 保留时间 |
|----------|------|----------|
| 全量备份 | 每周 | 1个月 |
| 增量备份 | 每日 | 1周 |

### 8.2 恢复流程

1. 停止写入
2. 恢复数据文件
3. 验证数据完整性
4. 恢复服务

---

## 九、使用示例

### 9.1 InfluxDB 查询

```flux
// 查询设备最新温度
from(bucket: "huakuantong_telemetry")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "device_telemetry")
  |> filter(fn: (r) => r.device_id == "1001")
  |> filter(fn: (r) => r._field == "temperature")
  |> last()

// 查询24小时温度趋势
from(bucket: "huakuantong_telemetry")
  |> range(start: -24h)
  |> filter(fn: (r) => r._field == "temperature")
  |> aggregateWindow(every: 1h, fn: mean)
```

### 9.2 TDengine 查询

```sql
-- 查询设备最新温度
SELECT last_row(*) FROM device_telemetry_s
WHERE device_id = '1001' AND metric_name = 'temperature';

-- 查询24小时温度趋势
SELECT ts, metric_value FROM device_telemetry_s
WHERE device_id = '1001'
  AND metric_name = 'temperature'
  AND ts > NOW - 24h
ORDER BY ts;
```
