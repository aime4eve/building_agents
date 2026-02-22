# 华宽通智能体系统 - DevOps环境搭建指南

## 目录

- [环境准备](#环境准备)
- [本地开发环境](#本地开发环境)
- [Kubernetes部署](#kubernetes部署)
- [CI/CD流水线](#cicd流水线)
- [监控配置](#监控配置)
- [常用命令](#常用命令)

---

## 环境准备

### 前置要求

- Docker 24.0+
- Docker Compose 2.20+
- Kubernetes 1.28+ (推荐使用 Minikube/Kind 进行本地测试)
- kubectl 1.28+
- Helm 3.12+ (可选)
- Git 2.40+

### 安装 Docker

#### Windows
```bash
# 下载并安装 Docker Desktop
# https://www.docker.com/products/docker-desktop
```

#### Linux
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
```

### 安装 Kubernetes (本地)

#### Minikube
```bash
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube
minikube start --driver=docker --cpus=4 --memory=8192
```

#### Kind
```bash
go install sigs.k8s.io/kind@latest
kind create cluster --name huakuantong-dev
```

---

## 本地开发环境

### 启动所有服务

```bash
cd devops/docker
docker-compose up -d
```

### 查看服务状态

```bash
docker-compose ps
```

### 查看服务日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f mysql
docker-compose logs -f rabbitmq
docker-compose logs -f emqx
```

### 停止所有服务

```bash
docker-compose down
```

### 停止服务并删除数据卷

```bash
docker-compose down -v
```

---

## 服务访问地址

| 服务 | 地址 | 用户名 | 密码 |
|------|------|--------|------|
| MySQL | localhost:3306 | root | root123456 |
| Redis | localhost:6379 | - | - |
| RabbitMQ | http://localhost:15672 | admin | admin123456 |
| EMQX | http://localhost:18083 | admin | admin123456 |
| Nacos | http://localhost:8848/nacos | nacos | nacos |
| Elasticsearch | http://localhost:9200 | - | - |
| Kibana | http://localhost:5601 | - | - |
| Prometheus | http://localhost:9090 | - | - |
| Grafana | http://localhost:3000 | admin | admin123456 |
| Zipkin | http://localhost:9411 | - | - |

---

## Kubernetes部署

### 创建命名空间

```bash
kubectl apply -f devops/k8s/base/
```

### 部署监控组件

```bash
kubectl apply -f devops/k8s/monitoring/
```

### 部署应用服务

```bash
kubectl apply -f devops/k8s/services/
```

### 查看部署状态

```bash
kubectl get pods -n huakuantong-dev
kubectl get services -n huakuantong-dev
```

### 查看日志

```bash
kubectl logs -f deployment/gateway -n huakuantong-dev
```

### 端口转发（本地访问）

```bash
# Grafana
kubectl port-forward -n huakuantong-dev svc/grafana 3000:3000

# Prometheus
kubectl port-forward -n huakuantong-dev svc/prometheus 9090:9090
```

---

## CI/CD流水线

### GitLab CI/CD

1. 将 `.gitlab-ci.yml` 文件放到项目根目录
2. 配置 GitLab Runner
3. 配置以下 CI/CD 变量：

| 变量名 | 说明 | 示例 |
|--------|------|------|
| CI_REGISTRY | Docker仓库地址 | harbor.huakuantong.com |
| CI_REGISTRY_USER | Docker用户名 | gitlab-ci |
| CI_REGISTRY_PASSWORD | Docker密码 | ******** |
| SONAR_HOST | SonarQube地址 | http://sonar.huakuantong.com |
| SONAR_TOKEN | SonarQube令牌 | ******** |

### Jenkins

1. 创建 Jenkins Pipeline 任务
2. 配置 Git 源码仓库
3. 配置以下凭据：

| 凭据ID | 类型 | 说明 |
|--------|------|------|
| harbor-credentials | 用户名密码 | Harbor登录凭据 |
| dev-kubeconfig | Secret文件 | 开发环境Kubeconfig |
| test-kubeconfig | Secret文件 | 测试环境Kubeconfig |
| prod-kubeconfig | Secret文件 | 生产环境Kubeconfig |
| sonar-token | Secret文本 | SonarQube令牌 |
| wechat-webhook | Secret文本 | 企业微信Webhook |

---

## 监控配置

### Spring Boot Actuator 集成

在微服务项目的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
```

将 `devops/monitoring/spring-boot-application.yml` 的配置添加到应用配置中。

### Grafana 仪表板

1. 登录 Grafana (admin/admin123456)
2. 导入预配置仪表板：`devops/docker/grafana/dashboards/`
3. 配置 Prometheus 数据源

### 告警规则

将告警规则配置到 Prometheus：

```bash
kubectl create configmap prometheus-rules \
  --from-file=devops/monitoring/alert-rules.yml \
  -n huakuantong-dev
```

---

## 常用命令

### Docker 相关

```bash
# 清理未使用的镜像
docker image prune -a

# 查看容器资源使用
docker stats

# 进入容器
docker exec -it huakuantong-mysql bash
```

### Kubernetes 相关

```bash
# 查看所有资源
kubectl get all -n huakuantong-dev

# 查看Pod详情
kubectl describe pod <pod-name> -n huakuantong-dev

# 扩缩容
kubectl scale deployment/gateway --replicas=3 -n huakuantong-dev

# 查看事件
kubectl get events -n huakuantong-dev --sort-by='.lastTimestamp'

# 查看配置
kubectl get configmap -n huakuantong-dev
kubectl get secret -n huakuantong-dev
```

### 故障排查

```bash
# 查看Pod日志
kubectl logs <pod-name> -n huakuantong-dev

# 查看上一个容器的日志（崩溃重启后）
kubectl logs <pod-name> -n huakuantong-dev --previous

# 执行命令进入Pod
kubectl exec -it <pod-name> -n huakuantong-dev -- /bin/bash

# 端口转发进行调试
kubectl port-forward <pod-name> 8080:8080 -n huakuantong-dev
```

---

## 目录结构

```
devops/
├── ci/                          # CI/CD配置
│   ├── .gitlab-ci.yml          # GitLab CI流水线
│   └── Jenkinsfile             # Jenkins流水线
├── docker/                      # Docker配置
│   ├── docker-compose.yml      # 开发环境编排
│   ├── docker-compose.prod.yml # 生产环境编排
│   ├── mysql/                  # MySQL配置
│   ├── redis/                  # Redis配置
│   ├── rabbitmq/               # RabbitMQ配置
│   ├── emqx/                   # EMQX配置
│   ├── elasticsearch/          # Elasticsearch配置
│   ├── kibana/                 # Kibana配置
│   ├── prometheus/             # Prometheus配置
│   └── grafana/                # Grafana配置
├── k8s/                         # Kubernetes配置
│   ├── base/                   # 基础配置
│   ├── services/               # 应用服务配置
│   └── monitoring/             # 监控组件配置
├── monitoring/                  # 监控配置
│   ├── spring-boot-application.yml  # Actuator配置
│   └── alert-rules.yml         # 告警规则
└── README.md                    # 本文档
```

---

## 环境变量

### 开发环境 (.env)

```bash
# MySQL
MYSQL_ROOT_PASSWORD=root123456
MYSQL_DATABASE=huakuantong

# Redis
REDIS_PASSWORD=

# RabbitMQ
RABBITMQ_DEFAULT_USER=admin
RABBITMQ_DEFAULT_PASS=admin123456

# EMQX
EMQX_DASHBOARD_DEFAULT_USERNAME=admin
EMQX_DASHBOARD_DEFAULT_PASSWORD=admin123456

# Nacos
NACOS_AUTH_TOKEN=SecretKey012345678901234567890123456789012345678901234567890123456789
```

### 生产环境

生产环境请使用密钥管理系统（如 Kubernetes Secrets、Vault）管理敏感信息。

---

## 下一步

1. 配置 Git 仓库并设置访问权限
2. 搭建 Harbor 镜像仓库
3. 配置 CI/CD 流水线并运行
4. 部署应用到 Kubernetes 集群
5. 配置监控告警规则
6. 编写部署文档和运维手册

---

## 联系方式

- DevOps团队: devops@huakuantong.com
- 问题反馈: https://github.com/huakuantong/ai-agent/issues
