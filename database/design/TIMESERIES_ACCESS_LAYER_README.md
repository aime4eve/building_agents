# 时序数据库访问层设计文档

## 版本信息
- 版本: V1.0
- 创建日期: 2026-02-20
- 包: com.huakuantong.iot.platform.infrastructure.timeseries

---

## 一、设计概述

时序数据库访问层提供统一的数据访问接口，支持 InfluxDB 2.x 和 TDengine 3.x 两种时序数据库。

### 架构设计

```
┌─────────────────────────────────────────────────────────┐
│                Application Layer                        │
│  ┌───────────────────────────────────────────────────┐  │
│  │         DeviceDataQueryService                    │  │
│  │  - 统一查询入口                                    │  │
│  │  - 查询路由：MySQL vs 时序数据库                   │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          │
         ┌────────────────┴────────────────┐
         │                                  │
         ▼                                  ▼
┌─────────────────────┐        ┌─────────────────────┐
│  Domain Layer       │        │  Domain Layer       │
│  DeviceRepository   │        │  DeviceTelemetry    │
│  (MySQL写侧)         │        │  ReadRepository     │
│                     │        │  DeviceEvent        │
│  - 设备聚合根        │        │  ReadRepository     │
│  - 最新快照          │        │                     │
└─────────────────────┘        └─────────────────────┘
                                              │
         ┌────────────────────────────────────┤
         │                                    │
         ▼                                    ▼
┌─────────────────────┐        ┌─────────────────────┐
│  InfluxDB 2.x       │        │  TDengine 3.x       │
│  ┌───────────────┐  │        │  ┌───────────────┐  │
│  │ InfluxDB      │  │        │  │ TDengine      │  │
│  │ Client        │  │        │  │ JDBC          │  │
│  └───────────────┘  │        │  └───────────────┘  │
│  - Flux Query     │        │  - SQL Query        │
│  - Write API      │        │  - 自动建表          │
└─────────────────────┘        └─────────────────────┘
```

---

## 二、仓储接口设计

### 2.1 DeviceTelemetryReadRepository

设备遥测数据读仓储接口，定义时序数据库查询规范。

**文件位置：** `timeseries_repository.java`

**核心方法：**

| 方法 | 说明 |
|------|------|
| `findLatestByDeviceId()` | 查询设备最新遥测数据 |
| `findByDeviceIdAndTimeRange()` | 查询时间范围数据 |
| `findLatestMetricValue()` | 查询指标最新值 |
| `aggregateMetric()` | 聚合统计 |
| `aggregateMetricByWindow()` | 时间窗口聚合 |
| `calculateDataIntegrity()` | 计算数据完整率 |
| `getDataQualityStats()` | 获取数据质量统计 |

### 2.2 DeviceEventReadRepository

设备事件读仓储接口，支持事件查询和统计。

**文件位置：** `device_event_read_repository.java`

**核心方法：**

| 方法 | 说明 |
|------|------|
| `findLatestByDeviceId()` | 查询设备最新事件 |
| `findByDeviceIdAndEventType()` | 按类型查询事件 |
| `findByDeviceIdAndEventLevel()` | 按级别查询事件 |
| `countByEventType()` | 统计事件数量（按类型） |
| `countByEventLevel()` | 统计事件数量（按级别） |
| `findUnacknowledgedAlarms()` | 查询未确认告警 |

---

## 三、配置类设计

### 3.1 InfluxDB 配置

**文件位置：** `influxdb_config.java`

**配置项：**

```yaml
timeseries:
  influx:
    enabled: true
    url: http://localhost:8086
    token: your-token
    org: huakuantong
    bucket: huakuantong_telemetry
```

**自动配置：**
- 创建 `InfluxDBClient` Bean
- 健康检查
- 连接池管理

### 3.2 TDengine 配置

**文件位置：** `tdengine_config.java`, `tdengine_properties.java`

**配置项：**

```yaml
timeseries:
  tdengine:
    enabled: true
    url: jdbc:TAOS://localhost:6030/telemetry
    username: root
    password: taosdata
    database: telemetry
    pool:
      min-idle: 5
      max-active: 50
```

**自动配置：**
- 创建 `DataSource` Bean
- 自动创建数据库（KEEP 90）
- 连接池配置

---

## 四、查询服务设计

### 4.1 DeviceDataQueryService

统一查询入口，根据查询类型自动路由到合适的数据源。

**文件位置：** `query_service.java`

**查询路由规则：**

| 查询类型 | 数据源 | 说明 |
|----------|--------|------|
| 设备当前状态 | MySQL | 从设备聚合根获取 |
| 历史趋势数据 | InfluxDB/TDengine | 时间范围查询 |
| 多设备聚合 | InfluxDB/TDengine | 批量查询+统计 |
| 事件统计 | InfluxDB/TDengine | 分组统计 |

**核心方法：**

```java
// 当前状态查询（MySQL）
DeviceStatusDTO getDeviceStatus(String deviceId)
List<DeviceStatusDTO> getSpaceDeviceStatus(String spaceId)

// 历史趋势查询（时序数据库）
List<DeviceTelemetry> getHistoricalTelemetry(HistoricalTelemetryQuery request)
List<TrendDataPoint> getTrend(TrendQuery request)

// 多设备聚合（时序数据库）
List<DeviceTelemetry> getSpaceLatestTelemetry(String spaceId)
SpaceMetricStatistics getSpaceMetricStatistics(SpaceMetricQuery request)

// 事件统计（时序数据库）
Map<String, Long> countDeviceEventsByType(DeviceEventStatisticsQuery request)
List<DeviceEvent> getUnacknowledgedAlarms(String tenantId, int limit)

// 综合查询
DeviceOverviewDTO getDeviceOverview(String deviceId)
```

---

## 五、实现类设计

### 5.1 InfluxDB 实现

**文件位置：** `influxdb_telemetry_repository_impl.java`

**特点：**
- 使用 Flux 查询语言
- 支持时间范围过滤
- 支持聚合窗口
- 支持数据删除

**查询示例：**

```flux
// 查询最新数据
from(bucket: "huakuantong_telemetry")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "device_telemetry")
  |> filter(fn: (r) => r.device_id == "1001")
  |> last()

// 时间窗口聚合
from(bucket: "huakuantong_telemetry")
  |> range(start: -24h)
  |> filter(fn: (r) => r._field == "temperature")
  |> aggregateWindow(every: 1h, fn: mean)
```

### 5.2 TDengine 实现

TDengine 实现（未在本次设计中展示，结构类似）使用：
- 标准 SQL 语法
- 自动创建子表
- 流式计算
- 超级表查询

---

## 六、使用示例

### 6.1 查询设备当前状态

```java
@Service
public class DeviceService {
    @Autowired
    private DeviceDataQueryService queryService;

    public DeviceStatusVO getDeviceStatus(String deviceId) {
        DeviceStatusDTO status = queryService.getDeviceStatus(deviceId);
        return DeviceStatusVO.from(status);
    }
}
```

### 6.2 查询历史趋势

```java
public List<TrendDataVO> getTemperatureTrend(String deviceId, int hours) {
    TrendQuery query = new TrendQuery();
    query.setDeviceId(deviceId);
    query.setMetricName("temperature");
    query.setFrom(LocalDateTime.now().minusHours(hours));
    query.setTo(LocalDateTime.now());
    query.setWindowSeconds(3600); // 1小时窗口

    List<TrendDataPoint> points = queryService.getTrend(query);
    return points.stream()
        .map(TrendDataVO::from)
        .collect(Collectors.toList());
}
```

### 6.3 查询空间内设备概览

```java
public SpaceDeviceOverviewVO getSpaceOverview(String spaceId) {
    List<DeviceStatusDTO> statusList =
        queryService.getSpaceDeviceStatus(spaceId);
    List<DeviceTelemetry> telemetryList =
        queryService.getSpaceLatestTelemetry(spaceId);

    return SpaceDeviceOverviewVO.create(statusList, telemetryList);
}
```

---

## 七、数据库切换

### 7.1 配置切换

```yaml
# 使用 InfluxDB
timeseries:
  influx:
    enabled: true
  tdengine:
    enabled: false

# 或使用 TDengine
timeseries:
  influx:
    enabled: false
  tdengine:
    enabled: true
```

### 7.2 实现切换

使用 `@ConditionalOnProperty` 控制实现类加载：

```java
@Repository
@ConditionalOnProperty(prefix = "timeseries.influx", name = "enabled", havingValue = "true")
public class InfluxDBDeviceTelemetryRepository implements DeviceTelemetryReadRepository {
    // InfluxDB 实现
}

@Repository
@ConditionalOnProperty(prefix = "timeseries.tdengine", name = "enabled", havingValue = "true")
public class TDengineDeviceTelemetryRepository implements DeviceTelemetryReadRepository {
    // TDengine 实现
}
```

---

## 八、性能优化建议

### 8.1 查询优化

1. **限制时间范围**：必须指定时间范围，避免全表扫描
2. **使用聚合**：大数据量场景使用聚合窗口减少返回数据量
3. **批量查询**：多设备查询使用批量接口

### 8.2 缓存策略

1. **设备当前状态**：Redis 缓存，TTL 5分钟
2. **最新遥测数据**：Redis 缓存，TTL 1分钟
3. **聚合统计**：Redis 缓存，TTL 15分钟

### 8.3 异步加载

```java
@Async
public CompletableFuture<List<DeviceTelemetry>> getHistoricalTelemetryAsync(
    HistoricalTelemetryQuery request
) {
    return CompletableFuture.completedFuture(
        getHistoricalTelemetry(request)
    );
}
```

---

## 九、监控与告警

### 9.1 监控指标

| 指标 | 说明 | 告警阈值 |
|------|------|----------|
| 查询响应时间 | 时序数据库查询耗时 | > 1s |
| 查询失败率 | 查询失败比例 | > 5% |
| 连接池使用率 | 连接池使用比例 | > 80% |

### 9.2 日志记录

```java
log.debug("Query telemetry: deviceId={}, from={}, to={}", deviceId, from, to);
log.info("Telemetry query completed: deviceId={}, resultCount={}", deviceId, results.size());
log.error("Failed to query telemetry", exception);
```

---

## 十、文件清单

| 文件 | 说明 |
|------|------|
| `timeseries_repository.java` | 设备遥测数据读仓储接口 |
| `device_event_read_repository.java` | 设备事件读仓储接口 |
| `influxdb_config.java` | InfluxDB 配置类 |
| `tdengine_config.java` | TDengine 配置类 |
| `tdengine_properties.java` | TDengine 配置属性 |
| `influxdb_telemetry_repository_impl.java` | InfluxDB 仓储实现类 |
| `query_service.java` | 统一查询服务 |
| `TIMESERIES_ACCESS_LAYER_README.md` | 本文档 |
