-- ============================================================================
-- 华宽通智能体系统 - 时序数据库设计
-- 版本: V1.0
-- 时序数据库: InfluxDB 2.x / TDengine 3.x
-- 创建日期: 2026-02-20
-- ============================================================================

-- 本文件提供两种时序数据库的DDL：
-- 1. InfluxDB 2.x Flux语法
-- 2. TDengine 3.x SQL语法

-- ============================================================================
-- 第一部分：InfluxDB 2.x 设计
-- ============================================================================

/*
-------------------------------------------------------------------------------
InfluxDB 2.x 数据组织结构
-------------------------------------------------------------------------------

Bucket: huakuantong_telemetry
├── Measurement: device_telemetry      (设备遥测数据)
├── Measurement: device_event          (设备事件数据)
└── Measurement: device_command        (设备命令记录)

-------------------------------------------------------------------------------
数据保留策略 (Retention Policy)
-------------------------------------------------------------------------------

策略名称         保留时间    分片时长    说明
------------------------------------------------------------
hot_rp           7 days     1 day      热数据，高频查询
warm_rp          30 days    7 days     温数据，常规查询
cold_rp          365 days   30 days    冷数据，归档查询
default_rp       90 days    7 days     默认策略
*/

-- ============================================================================
-- 1. 创建 Bucket
-- ============================================================================

# InfluxDB CLI 命令
influx bucket create \
  --name huakuantong_telemetry \
  --org huakuantong \
  --retention 90d \
  --type user

-- ============================================================================
-- 2. 设备遥测数据 Measurement: device_telemetry
-- ============================================================================

/*
数据结构:

Tag (索引字段，不可变):
  - tenant_id: 租户ID (string)
  - device_id: 设备ID (string)
  - device_sn: 设备序列号 (string)
  - device_type: 设备类型 (string)
  - device_model: 设备型号 (string)
  - space_id: 空间ID (string)
  - data_source: 数据来源 (string) - DEVICE/EDGE/CALCULATED

Field (测量值，可变):
  - 属性值: 根据物模型动态定义
  - 常见属性: temperature, humidity, pressure, power, voltage, current, etc.
  - data_quality: 数据质量 (int) - 1:好 2:一般 3:差

Timestamp: 时间戳 (纳秒精度)

示例数据:
  device_telemetry,tenant_id=1,device_id=1001,device_sn=SN001,device_type=sensor,device_model=T100,space_id=101,data_source=DEVICE \
    temperature=25.5,humidity=60.2,pressure=1013.25,data_quality=1 \
    1708395600000000000
*/

-- ============================================================================
-- 3. 设备事件数据 Measurement: device_event
-- ============================================================================

/*
数据结构:

Tag (索引字段):
  - tenant_id: 租户ID (string)
  - device_id: 设备ID (string)
  - device_sn: 设备序列号 (string)
  - event_type: 事件类型 (string) - ONLINE/OFFLINE/FAULT/ALERT/MAINTENANCE
  - event_level: 事件级别 (string) - INFO/WARNING/ERROR/CRITICAL
  - space_id: 空间ID (string)

Field (测量值):
  - event_message: 事件消息 (string)
  - event_code: 事件代码 (string)
  - event_value: 事件值 (float, 可选)
  - duration: 持续时间(ms, 可选)

Timestamp: 事件发生时间 (纳秒精度)

示例数据:
  device_event,tenant_id=1,device_id=1001,device_sn=SN001,event_type=ALERT,event_level=WARNING,space_id=101 \
    event_message="温度超过阈值",event_code="TEMP_HIGH",event_value=35.5,duration=300000 \
    1708395600000000000
*/

-- ============================================================================
-- 4. 设备命令记录 Measurement: device_command
-- ============================================================================

/*
数据结构:

Tag (索引字段):
  - tenant_id: 租户ID (string)
  - device_id: 设备ID (string)
  - device_sn: 设备序列号 (string)
  - command_type: 命令类型 (string)
  - command_status: 命令状态 (string) - PENDING/SENT/ACK/FAILED/TIMEOUT
  - operator_id: 操作人ID (string)

Field (测量值):
  - command_id: 命令ID (string)
  - service_identifier: 服务标识符 (string)
  - input_params: 输入参数 (string, JSON)
  - output_result: 输出结果 (string, JSON, 可选)
  - error_message: 错误信息 (string, 可选)
  - retry_count: 重试次数 (int)
  - execution_duration: 执行时长(ms) (int)

Timestamp: 命令发送时间 (纳秒精度)
*/

-- ============================================================================
-- 5. Flux 查询示例
-- ============================================================================

// 查询设备最新遥测数据
from(bucket: "huakuantong_telemetry")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "device_telemetry")
  |> filter(fn: (r) => r.device_id == "1001")
  |> last()

// 查询设备温度趋势（最近24小时，按小时聚合）
from(bucket: "huakuantong_telemetry")
  |> range(start: -24h)
  |> filter(fn: (r) => r._measurement == "device_telemetry")
  |> filter(fn: (r) => r._field == "temperature")
  |> filter(fn: (r) => r.device_id == "1001")
  |> aggregateWindow(every: 1h, fn: mean, createEmpty: false)
  |> yield(name: "avg_temperature")

// 查询设备事件统计（按事件类型分组）
from(bucket: "huakuantong_telemetry")
  |> range(start: -7d)
  |> filter(fn: (r) => r._measurement == "device_event")
  |> group(columns: ["event_type", "event_level"])
  |> count()

// 查询空间内所有设备的最新状态
from(bucket: "huakuantong_telemetry")
  |> range(start: -5m)
  |> filter(fn: (r) => r._measurement == "device_telemetry")
  |> filter(fn: (r) => r.space_id == "101")
  |> group(columns: ["device_id"])
  |> last()

// 计算设备数据上报频率
from(bucket: "huakuantong_telemetry")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "device_telemetry")
  |> filter(fn: (r) => r.device_id == "1001")
  |> count()
  |> map(fn: (r) => ({ r with _value: r._value / 3600.0 }))  // 每秒上报次数


-- ============================================================================
-- 第二部分：TDengine 3.x 设计
-- ============================================================================

-- ============================================================================
-- 1. 创建数据库
-- ============================================================================

-- 创建数据库，指定保留时间和副本数
CREATE DATABASE IF NOT EXISTS telemetry KEEP 90 UPDATE 1;

-- 使用数据库
USE telemetry;

-- ============================================================================
-- 2. 创建超级表：设备遥测数据
-- ============================================================================

/*
超级表结构:

STable: device_telemetry_s

Tag (标签):
  - tenant_id: 租户ID (NCHAR(50))
  - device_id: 设备ID (NCHAR(50))
  - device_sn: 设备序列号 (NCHAR(100))
  - device_type: 设备类型 (NCHAR(50))
  - device_model: 设备型号 (NCHAR(100))
  - space_id: 空间ID (NCHAR(50))
  - data_source: 数据来源 (NCHAR(20))

Field (列):
  - ts: TIMESTAMP - 时间戳（主键）
  - data_quality: INT - 数据质量
  - metric_name: NCHAR(100) - 指标名称
  - metric_value: DOUBLE - 指标值
  - metric_str: NCHAR(500) - 字符串值（可选）
*/

CREATE STABLE IF NOT EXISTS device_telemetry_s (
  ts TIMESTAMP,
  data_quality INT,
  metric_name NCHAR(100),
  metric_value DOUBLE,
  metric_str NCHAR(500)
) TAGS (
  tenant_id NCHAR(50),
  device_id NCHAR(50),
  device_sn NCHAR(100),
  device_type NCHAR(50),
  device_model NCHAR(100),
  space_id NCHAR(50),
  data_source NCHAR(20)
);

-- ============================================================================
-- 3. 创建超级表：设备事件数据
-- ============================================================================

CREATE STABLE IF NOT EXISTS device_event_s (
  ts TIMESTAMP,
  event_code NCHAR(50),
  event_message NCHAR(500),
  event_value DOUBLE,
  duration BIGINT,
  event_data NCHAR(1000)
) TAGS (
  tenant_id NCHAR(50),
  device_id NCHAR(50),
  device_sn NCHAR(100),
  event_type NCHAR(50),
  event_level NCHAR(20),
  space_id NCHAR(50)
);

-- ============================================================================
-- 4. 创建超级表：设备命令记录
-- ============================================================================

CREATE STABLE IF NOT EXISTS device_command_s (
  ts TIMESTAMP,
  command_id NCHAR(100),
  service_identifier NCHAR(100),
  input_params NCHAR(1000),
  output_result NCHAR(1000),
  error_message NCHAR(500),
  retry_count INT,
  execution_duration BIGINT,
  command_status NCHAR(20)
) TAGS (
  tenant_id NCHAR(50),
  device_id NCHAR(50),
  device_sn NCHAR(100),
  command_type NCHAR(50),
  operator_id NCHAR(50)
);

-- ============================================================================
-- 5. 创建子表示例（自动创建，无需手动）
-- ============================================================================

/*
当使用自动建表时，TDengine会根据设备ID自动创建子表：
  device_telemetry_1001 (设备ID=1001的遥测数据)
  device_telemetry_1002 (设备ID=1002的遥测数据)
  ...

可以使用以下语法自动创建子表并插入数据：
*/

-- INSERT INTO device_telemetry_1001 USING device_telemetry_s
-- TAGS ('1', '1001', 'SN001', 'sensor', 'T100', '101', 'DEVICE')
-- VALUES (NOW, 1, 'temperature', 25.5, NULL);

-- ============================================================================
-- 6. TDengine SQL 查询示例
-- ============================================================================

// 查询设备最新遥测数据
SELECT * FROM device_telemetry_s WHERE device_id = '1001' ORDER BY ts DESC LIMIT 1;

// 查询设备温度趋势（最近24小时）
SELECT ts, metric_value as temperature
FROM device_telemetry_s
WHERE device_id = '1001'
  AND metric_name = 'temperature'
  AND ts > NOW - 24h
ORDER BY ts;

// 查询平均温度（按小时聚合）
SELECT _wstart as time, AVG(metric_value) as avg_temp
FROM device_telemetry_s
WHERE device_id = '1001'
  AND metric_name = 'temperature'
  AND ts > NOW - 24h
INTERVAL(1h)
SLIDING(1h);

// 查询设备事件统计
SELECT event_type, event_level, COUNT(*) as event_count
FROM device_event_s
WHERE device_id = '1001'
  AND ts > NOW - 7d
GROUP BY event_type, event_level;

// 查询空间内所有设备的最新数据
SELECT * FROM device_telemetry_s
WHERE space_id = '101'
  AND ts > NOW - 5m
PARTITION BY device_id
ORDER BY ts DESC;

// 多表联合查询（设备遥测 + 设备事件）
SELECT
  t1.ts,
  t1.metric_name,
  t1.metric_value,
  t2.event_type,
  t2.event_message
FROM device_telemetry_s t1
  LEFT JOIN device_event_s t2 ON t1.device_id = t2.device_id AND t1.ts = t2.ts
WHERE t1.device_id = '1001'
  AND t1.ts > NOW - 1h;

// 连续查询（计算每小时平均值）
CREATE TABLE IF NOT EXISTS device_telemetry_hourly_avg AS
SELECT
  _wstart as window_start,
  device_id,
  metric_name,
  AVG(metric_value) as avg_value
FROM device_telemetry_s
WHERE ts > NOW - 1h
INTERVAL(1h)
SLIDING(1h);


-- ============================================================================
-- 第三部分：数据保留策略与分区
-- ============================================================================

/*
-------------------------------------------------------------------------------
InfluxDB 数据保留策略
-------------------------------------------------------------------------------

-- 7天热数据保留策略（高精度，1天分片）
influx retention create \
  --name hot_rp \
  --bucket huakuantong_telemetry \
  --org huakuantong \
  --duration 7d \
  --shard-duration 1d

-- 30天温数据保留策略（7天分片，降采样）
influx retention create \
  --name warm_rp \
  --bucket huakuantong_telemetry \
  --org huakuantong \
  --duration 30d \
  --shard-duration 7d

-- 365天冷数据保留策略（30天分片，进一步降采样）
influx retention create \
  --name cold_rp \
  --bucket huakuantong_telemetry \
  --org huakuantong \
  --duration 365d \
  --shard-duration 30d

-- 数据降采样任务（通过Task实现）
-- 示例：将热数据降采样后存入温数据
option task = {
  name: "downsample_to_warm",
  every: 1h,
  offset: 10m
}

from(bucket: "huakuantong_telemetry/hot_rp")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "device_telemetry")
  |> aggregateWindow(every: 5m, fn: mean, createEmpty: false)
  |> to(bucket: "huakuantong_telemetry/warm_rp", org: "huakuantong")
*/

/*
-------------------------------------------------------------------------------
TDengine 数据保留策略
-------------------------------------------------------------------------------

-- 创建不同保留时长的数据库
CREATE DATABASE IF NOT EXISTS telemetry_hot KEEP 7 UPDATE 1;
CREATE DATABASE IF NOT EXISTS telemetry_warm KEEP 30 UPDATE 1;
CREATE DATABASE IF NOT EXISTS telemetry_cold KEEP 365 UPDATE 1;

-- 数据迁移（使用流式计算）
-- 创建流，将热数据自动聚合后存入温数据
CREATE STREAM IF NOT EXISTS stream_to_warm AS
SELECT
  _wstart as ts,
  tenant_id,
  device_id,
  device_sn,
  device_type,
  device_model,
  space_id,
  data_source,
  AVG(metric_value) as metric_value,
  LAST(data_quality) as data_quality
FROM telemetry_hot.device_telemetry_s
WHERE ts > NOW - 1h
INTERVAL(5m) SLIDING(5m)
INTO telemetry_warm.device_telemetry_s;

-- 创建流，将温数据进一步聚合后存入冷数据
CREATE STREAM IF NOT EXISTS stream_to_cold AS
SELECT
  _wstart as ts,
  tenant_id,
  device_id,
  metric_name,
  AVG(metric_value) as metric_value
FROM telemetry_warm.device_telemetry_s
WHERE ts > NOW - 1d
INTERVAL(1h) SLIDING(1h)
INTO telemetry_cold.device_telemetry_s;
*/


-- ============================================================================
-- 第四部分：性能优化建议
-- ============================================================================

/*
-------------------------------------------------------------------------------
InfluxDB 性能优化
-------------------------------------------------------------------------------

1. Tag 设计原则:
   - Tag 是索引字段，数量不宜过多（建议<10个）
   - Tag 值基数不宜过高（避免高基数问题）
   - 将常用查询条件设为 Tag

2. Field 设计原则:
   - Field 是实际测量值，数量可多
   - 同一类型的值使用相同 Field 名称
   - 避免在 Field 中存储字符串

3. 数据写入优化:
   - 批量写入（建议每批5000-10000点）
   - 使用时间预分片
   - 避免高频小批量写入

4. 查询优化:
   - 限制时间范围
   - 使用 aggregateWindow 减少数据量
   - 避免全表扫描
*/

/*
-------------------------------------------------------------------------------
TDengine 性能优化
-------------------------------------------------------------------------------

1. 超级表设计原则:
   - Tag 列数量建议 < 10个
   - Tag 值应选择低基数字段
   - 每个子表对应一个设备

2. 数据写入优化:
   - 批量插入（建议每批1000-5000行）
   - 使用自动建表功能
   - 避免 N:N 子表数量过多（建议<100万）

3. 查询优化:
   - 利用 Tag 索引过滤
   - 使用 GROUP BY 减少数据量
   - 合理使用 PARTITION BY

4. 存储优化:
   - 设置合适的 KEEP 参数
   - 定期清理过期数据
   - 使用压缩参数
*/


-- ============================================================================
-- 第五部分：读写分离架构说明
-- ============================================================================

/*
-------------------------------------------------------------------------------
数据流向
-------------------------------------------------------------------------------

                         ┌─────────────────┐
                         │  设备数据上报    │
                         └────────┬────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    │             │             │
                    ▼             ▼             ▼
         ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
         │  写侧(MySQL)  │ │ 读侧(Influx) │ │读侧(TDengine)│
         │              │ │              │ │              │
         │ device_      │ │ device_      │ │ device_      │
         │ telemetry_   │ │ telemetry    │ │ telemetry_s  │
         │ snapshot     │ │ (Measurement)│ │ (SuperTable) │
         │              │ │              │ │              │
         │ 最新快照     │ │ 历史时序数据  │ │ 历史时序数据  │
         │ 每设备1条    │ │ 7-90天       │ │ 7-90天       │
         └──────────────┘ └──────────────┘ └──────────────┘
                    │             │             │
                    │             │             │
                    ▼             ▼             ▼
         ┌──────────────────────────────────────────┐
         │           应用层查询路由                  │
         │                                          │
         │  - 当前状态查询 → MySQL                  │
         │  - 历史趋势查询 → Influx/TDengine        │
         │  - 统计分析查询 → Influx/TDengine        │
         └──────────────────────────────────────────┘

-------------------------------------------------------------------------------
写入流程
-------------------------------------------------------------------------------

1. 设备上报数据
   ↓
2. 写入 MySQL (device_telemetry_snapshot)
   - 更新设备最新状态快照
   - 每设备只保留1条记录
   - 使用乐观锁保证一致性
   ↓
3. 异步写入时序数据库
   - 通过 Kafka 消息队列解耦
   - 批量写入提高性能
   - 保留完整历史数据

-------------------------------------------------------------------------------
查询流程
-------------------------------------------------------------------------------

查询类型              存储位置        查询示例
---------------------------------------------------------------------------
设备当前状态          MySQL          SELECT * FROM device_telemetry_snapshot
                                   WHERE device_id = ?
设备最新数据          MySQL          SELECT latest_properties FROM device
                                   WHERE id = ?
历史趋势分析          Influx/TDengine  SELECT ... WHERE time > now() - 24h
多设备数据聚合        Influx/TDengine  GROUP BY device_id, time(1h)
事件统计查询          Influx/TDengine  WHERE event_type = 'FAULT'
                                   GROUP BY event_level
*/
