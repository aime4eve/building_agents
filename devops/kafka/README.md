# EMQX 和 Kafka 配置说明

## 目录结构

```
devops/
├── docker/
│   ├── emqx.conf           # EMQX主配置文件
│   ├── acl.conf            # EMQX访问控制列表
│   ├── docker-compose-mqttkafka.yml  # Docker Compose配置
│   └── certs/              # 证书目录（需自行创建）
└── kafka/
    └── create-topics.sh    # Kafka Topic创建脚本
```

## 快速开始

### 1. 生成测试证书（开发环境）

```bash
cd devops/docker/certs

# 生成CA私钥
openssl genrsa -out ca.key 2048

# 生成CA证书
openssl req -new -x509 -days 3650 -key ca.key -out ca.crt -subj "/CN=HKT IoT CA"

# 生成服务器私钥
openssl genrsa -out server.key 2048

# 生成服务器CSR
openssl req -new -key server.key -out server.csr -subj "/CN=localhost"

# CA签名服务器证书
openssl x509 -req -days 3650 -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out server.crt

# 生成客户端私钥
openssl genrsa -out client.key 2048

# 生成客户端CSR
openssl req -new -key client.key -out client.csr -subj "/CN=device_test"

# CA签名客户端证书
openssl x509 -req -days 3650 -in client.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out client.crt
```

### 2. 启动服务

```bash
cd devops/docker

# 启动所有服务
docker-compose -f docker-compose-mqttkafka.yml up -d

# 查看服务状态
docker-compose -f docker-compose-mqttkafka.yml ps
```

### 3. 创建Kafka Topics

```bash
cd devops/kafka

# 赋予执行权限
chmod +x create-topics.sh

# 执行创建脚本
docker exec -i hkt-kafka1 bash < create-topics.sh
```

### 4. 访问管理界面

| 服务 | URL | 默认账号 |
|------|-----|----------|
| EMQX Dashboard | http://localhost:18083 | admin/public |
| Kafka UI | http://localhost:8080 | - |

## 配置说明

### EMQX 配置 (emqx.conf)

关键配置项：

| 配置项 | 值 | 说明 |
|--------|-----|------|
| MQTT/TCP | 1883 | 非加密连接 |
| MQTT/SSL | 8883 | TLS加密连接（生产环境） |
| MQTT/WS | 8083 | WebSocket连接 |
| MQTT/WSS | 8084 | WebSocket TLS连接 |
| 认证方式 | JWT + mTLS | 双重认证 |
| 集群方式 | manual | 手动集群模式 |

### Kafka 配置

| Topic | 分区数 | 保留时间 | 用途 |
|-------|--------|----------|------|
| device-telemetry | 50 | 30天 | 遥测数据 |
| device-event | 20 | 90天 | 设备事件 |
| device-status | 20 | 7天 | 设备状态 |
| device-command | 30 | 1天 | 命令下发 |
| device-alarm | 10 | 90天 | 告警数据 |
| device-ota | 5 | 30天 | OTA升级 |

### ACL 配置 (acl.conf)

权限规则：

- 设备只能发布自己的遥测/事件/状态数据
- 设备只能订阅自己的命令和OTA Topic
- 平台服务可以订阅所有设备数据
- 支持网关代表子设备发布消息

## 生产环境部署注意事项

1. **安全性**
   - 修改默认管理员密码
   - 使用生产环境签发的证书
   - 配置防火墙规则

2. **高可用**
   - 部署3节点EMQX集群
   - 部署3节点Kafka集群
   - 配置负载均衡器

3. **性能优化**
   - 根据设备数量调整Kafka分区数
   - 配置适当的消息保留策略
   - 启用消息压缩

4. **监控**
   - 配置Prometheus采集指标
   - 设置告警规则
   - 定期备份配置
