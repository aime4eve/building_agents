# EMQX + Kafka 配置使用指南

## 目录

- [EMQX配置](#emqx配置)
- [Kafka配置](#kafka配置)
- [监控配置](#监控配置)
- [快速启动](#快速启动)

---

## EMQX配置

### 配置文件

- `emqx/emqx.conf` - EMQX主配置文件
- `emqx/acl.conf` - 访问控制列表配置

### 核心配置项

#### 1. 监听器配置

```hocon
listeners {
  tcp.default {
    bind = "0.0.0.0:1883"
    max_connections = 1024000
  }

  ssl.external {
    bind = "0.0.0.0:8883"
    ssl_options {
      keyfile = "/etc/certs/key.pem"
      certfile = "/etc/certs/cert.pem"
      cacertfile = "/etc/certs/cacert.pem"
    }
  }
}
```

#### 2. JWT认证配置

```hocon
authentication {
  backend = jwt
  algorithm = rs256
  secret = "${JWT_SECRET}"
  from = username
  verify_claims = {
    "device_id" = {optional = false}
    "tenant_id" = {optional = false}
  }
}
```

#### 3. Kafka桥接配置

```hocon
bridges.kafka {
  device_telemetry_producer {
    enable = true
    bootstrap_servers = "kafka1:9092,kafka2:9093,kafka3:9094"
    routes = [
      {
        topic = "device-telemetry"
        from = "device/+/+/+/telemetry"
        key = "${clientid}"
      }
    ]
  }
}
```

### Dashboard访问

| 项目 | 值 |
|------|-----|
| URL | http://localhost:18083 |
| 用户名 | admin |
| 密码 | admin123456 |

---

## Kafka配置

### 配置文件

- `kafka/server.properties` - Kafka Broker配置
- `kafka/topics-create.sh` - Topic创建脚本
- `kafka/consumer-groups-create.sh` - 消费者组创建脚本

### Topic配置

| Topic | 分区数 | 副本数 | 保留时间 | 用途 |
|-------|--------|--------|----------|------|
| device-telemetry | 50 | 3 | 30天 | 遥测数据 |
| device-event | 20 | 3 | 90天 | 设备事件 |
| device-status | 30 | 3 | 7天 | 设备状态 |
| device-command | 30 | 3 | 1天 | 命令下发 |
| device-heartbeat | 20 | 3 | 7天 | 设备心跳 |
| device-ota | 10 | 3 | 30天 | OTA升级 |
| device-auth | 10 | 3 | 30天 | 认证日志 |

### 消费者组

| 消费者组 | 用途 |
|----------|------|
| telemetry-processor-group | 遥测数据处理 |
| event-processor-group | 事件处理 |
| status-processor-group | 状态处理 |
| command-bridge-group | EMQX桥接 |
| heartbeat-processor-group | 心跳处理 |
| ota-processor-group | OTA处理 |

---

## 监控配置

### Prometheus监控

#### EMQX指标采集

```yaml
scrape_configs:
  - job_name: 'emqx'
    static_configs:
      - targets: ['emqx:18083']
    metrics_path: '/api/v5/prometheus/stats'
```

#### Kafka指标采集

```yaml
scrape_configs:
  - job_name: 'kafka-exporter'
    static_configs:
      - targets: ['kafka-exporter:9308']

  - job_name: 'kafka-jmx'
    static_configs:
      - targets: ['kafka-jmx-exporter:5556']
```

### Grafana仪表板

导入仪表板JSON文件：

1. 登录Grafana (http://localhost:3000)
2. 导航到 Dashboards -> Import
3. 上传JSON文件或粘贴内容

可用仪表板：

- `emqx-dashboard.json` - EMQX监控
- `kafka-dashboard.json` - Kafka监控

---

## 快速启动

### 1. 启动服务

```bash
cd devops/docker
docker-compose -f docker-compose-mqttkafka.yml up -d
```

### 2. 等待服务就绪

```bash
# 检查服务状态
docker-compose -f docker-compose-mqttkafka.yml ps

# 查看EMQX日志
docker-compose -f docker-compose-mqttkafka.yml logs -f emqx1

# 查看Kafka日志
docker-compose -f docker-compose-mqttkafka.yml logs -f kafka1
```

### 3. 创建Kafka Topics

```bash
# 进入Kafka容器
docker exec -it hkt-kafka1 bash

# 创建Topics
bash /opt/kafka/topics-create.sh

# 创建消费者组
bash /opt/kafka/consumer-groups-create.sh
```

### 4. 验证配置

#### EMQX验证

```bash
# 访问Dashboard
# http://localhost:18083

# 查看连接的客户端
# Dashboard -> Monitoring -> Clients
```

#### Kafka验证

```bash
# 查看Topics
kafka-topics.sh --list --bootstrap-server localhost:9092

# 查看Topic详情
kafka-topics.sh --describe --bootstrap-server localhost:9092

# 访问Kafka UI
# http://localhost:8080
```

#### 监控验证

```bash
# 检查EMQX指标
curl http://localhost:18083/api/v5/prometheus/stats

# 检查Kafka Exporter指标
curl http://localhost:9308/metrics

# 访问Grafana查看仪表板
# http://localhost:3000
```

---

## 设备接入示例

### MQTT连接示例（mosquitto_pub）

```bash
# 发布遥测数据
mosquitto_pub -h localhost -p 1883 \
  -u "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -t "device/tenant001/temperature_sensor/dev_123456/telemetry" \
  -m '{
    "msgId": "msg_001",
    "deviceId": "dev_123456",
    "timestamp": 1708416000000,
    "data": {
      "temperature": 25.5,
      "humidity": 60.2
    }
  }'
```

### MQTT连接示例（Python）

```python
import paho.mqtt.client as mqtt
import json
import time

# JWT Token
token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

# 创建客户端
client = mqtt.Client()
client.username_pw_set(token, "")

# 连接回调
def on_connect(client, userdata, flags, rc):
    print(f"Connected with result code {rc}")
    # 订阅命令Topic
    client.subscribe("device/tenant001/+/+/command")

# 消息回调
def on_message(client, userdata, msg):
    print(f"Received: {msg.topic} - {msg.payload}")

client.on_connect = on_connect
client.on_message = on_message

# 连接
client.connect("localhost", 1883, 60)
client.loop_start()

# 发布遥测数据
telemetry = {
    "msgId": f"msg_{int(time.time())}",
    "deviceId": "dev_123456",
    "deviceType": "TEMPERATURE_SENSOR",
    "timestamp": int(time.time() * 1000),
    "data": {
        "temperature": 25.5,
        "humidity": 60.2
    }
}

client.publish(
    "device/tenant001/temperature_sensor/dev_123456/telemetry",
    json.dumps(telemetry)
)

time.sleep(1)
client.loop_stop()
```

---

## 故障排查

### EMQX连接失败

```bash
# 检查EMQX状态
docker exec hkt-emqx1 emqx ping

# 查看EMQX日志
docker logs hkt-emqx1 --tail 100

# 检查监听端口
docker exec hkt-emqx1 netstat -tlnp | grep emqx
```

### Kafka消息积压

```bash
# 查看消费者组状态
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --describe --group telemetry-processor-group

# 重置消费者offset
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group telemetry-processor-group \
  --reset-offsets --to-earliest \
  --topic device-telemetry --execute
```

### 监控数据缺失

```bash
# 检查Prometheus targets
curl http://localhost:9090/api/v1/targets

# 检查Kafka Exporter
curl http://localhost:9308/metrics | grep kafka_consumergroup_lag

# 检查EMQX Prometheus endpoint
curl http://localhost:18083/api/v5/prometheus/stats
```

---

## 目录结构

```
devops/iot/
├── emqx/
│   ├── emqx.conf           # EMQX主配置
│   └── acl.conf            # ACL规则
├── kafka/
│   ├── server.properties   # Kafka配置
│   ├── topics-create.sh    # Topic创建脚本
│   ├── consumer-groups-create.sh  # 消费者组创建脚本
│   └── jmx-exporter-config.yml    # JMX Exporter配置
├── grafana/
│   ├── emqx-dashboard.json # EMQX仪表板
│   └── kafka-dashboard.json # Kafka仪表板
└── README.md               # 本文档
```

---

## 参考文档

- [EMQX官方文档](https://www.emqx.io/docs/)
- [Kafka官方文档](https://kafka.apache.org/documentation/)
- [设备接入层架构设计说明书](D:/ai-agentic/docs/device-layer/设备接入层架构设计说明书.md)
