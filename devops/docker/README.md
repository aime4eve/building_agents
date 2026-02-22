# 时序数据库 Docker 部署指南

## 版本信息
- 版本: V1.0
- 创建日期: 2026-02-20

---

## 目录
1. [快速启动](#快速启动)
2. [InfluxDB 配置](#influxdb-配置)
3. [TDengine 配置](#tdengine-配置)
4. [数据持久化](#数据持久化)
5. [健康检查](#健康检查)
6. [使用示例](#使用示例)

---

## 快速启动

### 方案一：使用 InfluxDB

```bash
# 启动 InfluxDB（默认方案）
docker-compose -f docker-compose-timeseries.yml up -d

# 验证服务
docker-compose -f docker-compose-timeseries.yml ps

# 查看日志
docker-compose -f docker-compose-timeseries.yml logs -f influxdb
```

### 方案二：使用 TDengine

```bash
# 启动 TDengine
docker-compose -f docker-compose-timeseries.yml --profile tdengine up -d

# 验证服务
docker-compose -f docker-compose-timeseries.yml ps tdengine
```

### 启动完整监控栈

```bash
# 启动 InfluxDB + Grafana
docker-compose -f docker-compose-timeseries.yml --profile monitoring --profile ui up -d
```

---

## InfluxDB 配置

### 默认账号信息

| 项目 | 值 |
|------|-----|
| 用户名 | admin |
| 密码 | admin123456 |
| 组织 | huakuantong |
| 存储桶 | huakuantong_telemetry |
| 令牌 | my-super-secret-auth-token |
| 保留策略 | 90天 |

### 连接信息

| 项目 | 值 |
|------|-----|
| HTTP端口 | 8086 |
| Web UI | http://localhost:8086 |
| API地址 | http://localhost:8086 |

### 初始化脚本

首次启动时，InfluxDB会自动执行以下初始化：

```bash
# 创建组织
influx org create huakuantong

# 创建存储桶（90天保留）
influx bucket create --org huakuantong --retention 90d huakuantong_telemetry

# 创建API令牌
influx auth create --org huakuantong --write-bucket huakuantong_telemetry
```

### 手动初始化（可选）

```bash
# 进入容器
docker exec -it hkt-influxdb bash

# 执行初始化脚本
influx setup \
  --username admin \
  --password admin123456 \
  --org huakuantong \
  --bucket huakuantong_telemetry \
  --token my-super-secret-auth-token \
  --retention 90d \
  --force
```

---

## TDengine 配置

### 连接信息

| 项目 | 值 |
|------|-----|
| TCP端口 | 6030 |
| RESTful端口 | 6041 |
| 用户名 | root |
| 密码 | taosdata |

### 创建数据库

```bash
# 进入容器
docker exec -it hkt-tdengine taos

# 创建数据库（90天保留）
CREATE DATABASE IF NOT EXISTS telemetry KEEP 90 UPDATE 1;

# 使用数据库
USE telemetry;

# 查看数据库
SHOW DATABASES;
```

### 验证连接

```bash
# 方式1：使用taos命令行
docker exec -it hkt-tdengine taos -s "show databases;"

# 方式2：使用RESTful API
curl -u root:taosdata http://localhost:6041/rest/sql/"show databases"
```

---

## 数据持久化

### 数据卷映射

| 服务 | 数据卷 | 说明 |
|------|--------|------|
| influxdb | influxdb-data | InfluxDB数据目录 |
| influxdb | influxdb-config | InfluxDB配置目录 |
| tdengine | tdengine-data | TDengine数据目录 |
| tdengine | tdengine-config | TDengine配置目录 |
| mysql | mysql-data | MySQL数据目录 |
| redis | redis-data | Redis数据目录 |
| grafana | grafana-data | Grafana数据目录 |

### 备份数据

```bash
# InfluxDB 备份
docker exec hkt-influxdb influx backup /backup/backup-$(date +%Y%m%d)

# TDengine 备份
docker exec hkt-tdengine taosdump /backup/backup-$(date +%Y%m%d)

# MySQL 备份
docker exec hkt-mysql mysqldump -u root -p huakuantong_agent > backup.sql
```

---

## 健康检查

### InfluxDB 健康检查

```bash
# 检查服务状态
docker exec hkt-influxdb influx ping

# 查看健康状态
curl -I http://localhost:8086/health
```

### TDengine 健康检查

```bash
# 检查服务状态
docker exec hkt-tdengine taos -s "show databases;"

# 查看日志
docker logs hkt-tdengine --tail 100
```

### 所有服务状态

```bash
# 查看所有容器状态
docker-compose -f docker-compose-timeseries.yml ps

# 查看资源使用情况
docker stats
```

---

## 使用示例

### InfluxDB 使用示例

#### 1. 写入数据

```bash
# 使用 influx CLI
docker exec -it hkt-influxdb influx

# 写入数据（Flux格式）
device_telemetry,tenant_id=1,device_id=1001,device_type=sensor temperature=25.5,humidity=60.2 1708395600000000000
```

#### 2. 查询数据

```flux
# 查询最新数据
from(bucket: "huakuantong_telemetry")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "device_telemetry")
  |> last()

# 查询温度趋势
from(bucket: "huakuantong_telemetry")
  |> range(start: -24h)
  |> filter(fn: (r) => r._field == "temperature")
  |> aggregateWindow(every: 1h, fn: mean)
```

### TDengine 使用示例

#### 1. 创建超级表

```sql
-- 创建设备遥测数据超级表
CREATE STABLE IF NOT EXISTS device_telemetry_s (
  ts TIMESTAMP,
  data_quality INT,
  metric_name NCHAR(100),
  metric_value DOUBLE
) TAGS (
  tenant_id NCHAR(50),
  device_id NCHAR(50),
  device_sn NCHAR(100),
  device_type NCHAR(50)
);
```

#### 2. 写入数据

```sql
-- 自动创建子表并插入数据
INSERT INTO device_telemetry_1001 USING device_telemetry_s
TAGS ('1', '1001', 'SN001', 'sensor')
VALUES (NOW, 1, 'temperature', 25.5);
```

#### 3. 查询数据

```sql
-- 查询最新数据
SELECT * FROM device_telemetry_s
WHERE device_id = '1001'
ORDER BY ts DESC
LIMIT 1;

-- 查询24小时数据
SELECT ts, metric_value
FROM device_telemetry_s
WHERE device_id = '1001'
  AND metric_name = 'temperature'
  AND ts > NOW - 24h
ORDER BY ts;
```

---

## Spring Boot 配置

### application.yml 配置

```yaml
# InfluxDB 配置
timeseries:
  influx:
    enabled: true
    url: http://localhost:8086
    token: my-super-secret-auth-token
    org: huakuantong
    bucket: huakuantong_telemetry

# TDengine 配置（与InfluxDB二选一）
# timeseries:
#   tdengine:
#     enabled: true
#     url: jdbc:TAOS://localhost:6030/telemetry
#     username: root
#     password: taosdata
#     database: telemetry

# MySQL 配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/huakuantong_agent?useUnicode=true&characterEncoding=utf8mb4
    username: huakuantong
    password: hkt123456

# Redis 配置
  data:
    redis:
      host: localhost
      port: 6379
      password: redis123
```

---

## 常见问题

### Q1: InfluxDB 启动失败？

```bash
# 查看日志
docker logs hkt-influxdb

# 重置数据卷
docker-compose -f docker-compose-timeseries.yml down -v
docker-compose -f docker-compose-timeseries.yml up -d
```

### Q2: TDengine 连接失败？

```bash
# 检查端口
netstat -an | grep 6030

# 检查防火墙
sudo ufw allow 6030/tcp
```

### Q3: 数据没有持久化？

```bash
# 检查数据卷
docker volume inspect influxdb-data

# 备份数据
docker run --rm -v influxdb-data:/data -v $(pwd):/backup alpine tar czf /backup/influxdb-backup.tar.gz -C /data .
```

---

## 运维脚本

### 启动服务

```bash
#!/bin/bash
# start-timeseries.sh

echo "Starting InfluxDB..."
docker-compose -f docker-compose-timeseries.yml up -d influxdb

echo "Waiting for InfluxDB to be ready..."
sleep 30

echo "Initializing InfluxDB..."
docker exec hkt-influxdb influx setup \
  --username admin \
  --password admin123456 \
  --org huakuantong \
  --bucket huakuantong_telemetry \
  --retention 90d \
  --token my-super-secret-auth-token \
  --force

echo "InfluxDB started successfully!"
echo "Web UI: http://localhost:8086"
```

### 停止服务

```bash
#!/bin/bash
# stop-timeseries.sh

docker-compose -f docker-compose-timeseries.yml down

echo "Timeseries databases stopped."
```

### 监控脚本

```bash
#!/bin/bash
# monitor-timeseries.sh

while true; do
  echo "=== $(date) ==="
  echo "InfluxDB:"
  curl -s http://localhost:8086/health | jq .
  echo ""
  echo "TDengine:"
  docker exec hkt-tdengine taos -s "show databases;"
  echo ""
  sleep 60
done
```
