# HKT IoT Platform 部署文档

## 目录

- [环境要求](#环境要求)
- [本地开发环境](#本地开发环境)
- [Docker部署](#docker部署)
- [Kubernetes部署](#kubernetes部署)
- [监控与日志](#监控与日志)
- [故障排查](#故障排查)

---

## 环境要求

### 硬件要求

| 组件 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 4核 | 8核+ |
| 内存 | 16GB | 32GB+ |
| 磁盘 | 200GB | 500GB+ SSD |

### 软件要求

| 软件 | 版本 | 说明 |
|------|------|------|
| Docker | 20.10+ | 容器运行时 |
| Docker Compose | 2.0+ | 容器编排 |
| Kubernetes | 1.25+ | 容器编排（生产环境） |
| Helm | 3.0+ | K8s包管理 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.0+ | 缓存 |
| RabbitMQ | 3.12+ | 消息队列 |
| EMQX | 5.4+ | MQTT Broker |
| InfluxDB | 2.7+ | 时序数据库 |

---

## 本地开发环境

### 1. 启动基础设施

```bash
cd deploy/docker
docker-compose up -d
```

### 2. 初始化数据库

```bash
# 执行各服务的数据库初始化脚本
mysql -u root -p < init-db/nacos_config.sql
mysql -u root -p < init-db/hkt_iot_user.sql
mysql -u root -p < init-db/hkt_iot_device.sql
mysql -u root -p < init-db/hkt_iot_notification.sql
# ... 其他服务数据库
```

### 3. 启动微服务

```bash
# 方式1: IDE中启动各个服务
# 方式2: Maven命令启动
mvn spring-boot:run -pl hkt-iot-gateway
mvn spring-boot:run -pl hkt-iot-user-service
mvn spring-boot:run -pl hkt-iot-device-service
# ... 其他服务

# 方式3: 使用Docker Compose
docker-compose -f docker-compose-services.yml up -d
```

### 4. 访问服务

| 服务 | 地址 | 用户名 | 密码 |
|------|------|--------|------|
| API网关 | http://localhost:8080 | - | - |
| Nacos控制台 | http://localhost:8848/nacos | nacos | nacos |
| RabbitMQ管理界面 | http://localhost:15672 | admin | admin123 |
| EMQX Dashboard | http://localhost:18083 | admin | public |
| Grafana | http://localhost:3000 | admin | admin123 |
| Prometheus | http://localhost:9090 | - | - |

---

## Docker部署

### 1. 构建镜像

```bash
# 构建所有服务镜像
./scripts/build-all-images.sh

# 构建单个服务镜像
docker build -t hkt-iot/user-service:1.0.0 -f hkt-iot-user-service/Dockerfile .
```

### 2. 启动基础设施

```bash
cd deploy/docker
docker-compose up -d
```

### 3. 启动微服务

```bash
docker-compose -f docker-compose-services.yml up -d
```

### 4. 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f user-service
```

### 5. 停止服务

```bash
# 停止微服务
docker-compose -f docker-compose-services.yml down

# 停止基础设施
docker-compose down

# 停止所有并删除数据卷
docker-compose down -v
```

---

## Kubernetes部署

### 1. 准备K8s集群

```bash
# 创建命名空间
kubectl create namespace hkt-iot

# 创建ConfigMap和Secret
kubectl apply -f deploy/k8s/00-common.yaml
```

### 2. 部署基础设施

```bash
# 部署核心基础设施（MySQL、Redis、RabbitMQ）
kubectl apply -f deploy/k8s/01-infra.yaml

# 部署扩展基础设施（EMQX、InfluxDB、Nacos）
kubectl apply -f deploy/k8s/02-infra-ext.yaml

# 等待Pod就绪
kubectl wait --for=condition=ready pod -l app=mysql -n hkt-iot --timeout=300s
kubectl wait --for=condition=ready pod -l app=redis -n hkt-iot --timeout=300s
kubectl wait --for=condition=ready pod -l app=rabbitmq -n hkt-iot --timeout=300s
```

### 3. 部署微服务

```bash
# 部署用户服务
kubectl apply -f deploy/k8s/10-user-service.yaml

# 部署通知服务
kubectl apply -f deploy/k8s/11-notification-service.yaml

# 部署其他服务
kubectl apply -f deploy/k8s/20-device-service.yaml
kubectl apply -f deploy/k8s/30-rule-service.yaml
kubectl apply -f deploy/k8s/40-scene-service.yaml
```

### 4. 查看部署状态

```bash
# 查看所有Pod
kubectl get pods -n hkt-iot

# 查看服务
kubectl get svc -n hkt-iot

# 查看部署状态
kubectl get deployments -n hkt-iot

# 查看日志
kubectl logs -f deployment/user-service -n hkt-iot
```

### 5. 扩缩容

```bash
# 手动扩容
kubectl scale deployment user-service --replicas=5 -n hkt-iot

# 自动扩容（HPA）
kubectl autoscale deployment user-service --min=2 --max=10 --cpu-percent=70 -n hkt-iot
```

---

## 监控与日志

### Prometheus监控

访问地址：http://localhost:9090

配置文件：`deploy/docker/prometheus/prometheus.yml`

### Grafana仪表板

访问地址：http://localhost:3000

默认账号：admin / admin123

仪表板位置：`deploy/grafana/provisioning/dashboards/`

### ELK日志

| 服务 | 地址 | 用途 |
|------|------|------|
| Elasticsearch | http://localhost:9200 | 日志存储 |
| Logstash | http://localhost:5044 | 日志收集 |
| Kibana | http://localhost:5601 | 日志分析 |

---

## 故障排查

### 1. 服务启动失败

```bash
# 查看Pod状态
kubectl describe pod <pod-name> -n hkt-iot

# 查看日志
kubectl logs <pod-name> -n hkt-iot

# 查看事件
kubectl get events -n hkt-iot --sort-by='.lastTimestamp'
```

### 2. 数据库连接失败

```bash
# 检查数据库服务
kubectl get pods -l app=mysql -n hkt-iot

# 检查数据库连接
kubectl exec -it <mysql-pod> -n hkt-iot -- mysql -u root -p

# 检查网络连接
kubectl exec -it <service-pod> -n hkt-iot -- nc -zv mysql-service 3306
```

### 3. 消息队列问题

```bash
# 进入RabbitMQ管理界面
# 检查队列状态和连接

# 检查队列积压
curl -u admin:admin123 http://localhost:15672/api/queues
```

### 4. 高CPU/内存使用

```bash
# 查看资源使用
kubectl top pods -n hkt-iot
kubectl top nodes

# 调整资源限制
kubectl edit deployment <service-name> -n hkt-iot
```

---

## 备份与恢复

### 数据库备份

```bash
# 备份MySQL
kubectl exec -it <mysql-pod> -n hkt-iot -- mysqldump -u root -p --all-databases > backup.sql

# 恢复MySQL
kubectl exec -i <mysql-pod> -n hkt-iot -- mysql -u root -p < backup.sql
```

### InfluxDB备份

```bash
# 备份InfluxDB
influx backup /backup --bucket <bucket-name> --org <org-name>

# 恢复InfluxDB
influx restore /backup --bucket <bucket-name> --org <org-name>
```

---

## 安全配置

### 1. SSL/TLS配置

```bash
# 生成TLS证书
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes

# 配置Ingress使用TLS
kubectl apply -f deploy/k8s/ingress-tls.yaml
```

### 2. 网络策略

```bash
# 应用网络策略
kubectl apply -f deploy/k8s/network-policies.yaml
```

### 3. 密钥管理

```bash
# 创建Secret
kubectl create secret generic my-secret --from-literal=password=mypassword -n hkt-iot

# 加密Secret（使用Sealed Secrets）
kubeseal -f my-secret.yaml -w my-sealed-secret.yaml
```

---

## 多租户配置

### 1. 租户资源配额

```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: tenant-quota
  namespace: hkt-iot-tenant-001
spec:
  hard:
    requests.cpu: "4"
    requests.memory: 8Gi
    limits.cpu: "8"
    limits.memory: 16Gi
    persistentvolumeclaims: "5"
```

### 2. 租户限流

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tenant-ingress
  namespace: hkt-iot-tenant-001
  annotations:
    nginx.ingress.kubernetes.io/limit-rps: "100"
    nginx.ingress.kubernetes.io/limit-connections: "50"
spec:
  # ... ingress配置
```

---

## 附录

### 端口映射

| 服务 | 内部端口 | 外部端口 |
|------|----------|----------|
| Gateway | 8080 | 8080 |
| User Service | 8081 | - |
| Device Service | 8082 | - |
| Rule Service | 8084 | - |
| Scene Service | 8085 | - |
| Notification Service | 8086 | - |
| Nacos | 8848 | 8848 |
| MySQL | 3306 | 3306 |
| Redis | 6379 | 6379 |
| RabbitMQ | 5672/15672 | 5672/15672 |
| EMQX | 1883/18083 | 1883/18083 |
| InfluxDB | 8086 | 8086 |

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SPRING_PROFILES_ACTIVE | Spring环境 | dev |
| NACOS_ADDR | Nacos地址 | localhost:8848 |
| MYSQL_HOST | MySQL主机 | localhost |
| MYSQL_PORT | MySQL端口 | 3306 |
| REDIS_HOST | Redis主机 | localhost |
| REDIS_PORT | Redis端口 | 6379 |
| RABBITMQ_HOST | RabbitMQ主机 | localhost |
| RABBITMQ_PORT | RabbitMQ端口 | 5672 |
