# Camunda部署配置使用指南

## 目录

- [部署架构](#部署架构)
- [快速启动](#快速启动)
- [访问地址](#访问地址)
- [配置说明](#配置说明)
- [监控配置](#监控配置)

---

## 部署架构

### 服务组件

```
┌─────────────────────────────────────────────────────────────┐
│                    workflow-engine-service                   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  REST API Layer (Spring MVC)                        │   │
│  │  Port: 8085                                          │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Camunda Spring Boot Starter                        │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ REST API
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                  Camunda Platform 7                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Camunda BPM Engine                                  │   │
│  │  - Repository Service                                │   │
│  │  - Runtime Service                                   │   │
│  │  - Task Service                                      │   │
│  │  - History Service                                   │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Web Apps                                            │   │
│  │  - Cockpit (流程监控)                                │   │
│  │  - Tasklist (任务管理)                              │   │
│  │  - Admin (系统管理)                                  │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ JDBC
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                  PostgreSQL Database                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Camunda Core Tables                                  │   │
│  │  - ACT_RE_* (Repository Tables)                      │   │
│  │  - ACT_RU_* (Runtime Tables)                         │   │
│  │  - ACT_HI_* (History Tables)                          │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Business Extension Tables                           │   │
│  │  - wf_process_instance                               │   │
│  │  - wf_task                                           │   │
│  │  - wf_sla_config                                     │   │
│  │  - wf_sla_monitor                                    │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 快速启动

### 1. 启动Camunda服务

```bash
cd devops/camunda
docker-compose up -d
```

### 2. 查看服务状态

```bash
docker-compose ps
```

### 3. 查看日志

```bash
# 查看Camunda日志
docker-compose logs -f camunda

# 查看Workflow Engine Service日志
docker-compose logs -f workflow-engine-service

# 查看PostgreSQL日志
docker-compose logs -f postgres-camunda
```

### 4. 停止服务

```bash
docker-compose down
```

### 5. 停止服务并删除数据卷

```bash
docker-compose down -v
```

---

## 访问地址

| 服务 | 地址 | 用户名 | 密码 | 说明 |
|------|------|--------|------|------|
| Camunda Cockpit | http://localhost:8080/camunda/app/cockpit | admin | admin123456 | 流程监控 |
| Camunda Tasklist | http://localhost:8080/camunda/app/tasklist | admin | admin123456 | 任务管理 |
| Camunda Admin | http://localhost:8080/camunda/app/admin | admin | admin123456 | 系统管理 |
| Workflow Engine API | http://localhost:8085 | - | - | REST API |
| Workflow Engine Actuator | http://localhost:8085/actuator | - | - | 健康检查 |

### Camunda Modeler (独立桌面应用)

1. 下载地址: https://camunda.com/download/modeler/
2. 安装后启动，访问 http://localhost:26101
3. 用于创建和编辑BPMN流程定义

---

## 配置说明

### Workflow Engine Service配置

```yaml
# application.yml
camunda:
  bpm:
    url: http://camunda:8080/camunda
    client-id: workflow-engine
    client-secret: workflow-engine-secret
    database:
      schema-update: true

spring:
  datasource:
    url: jdbc:postgresql://postgres-camunda:5432/camunda
    username: camunda
    password: camunda123456

  rabbitmq:
    host: rabbitmq
    port: 5672
    username: admin
    password: admin123456
```

### 数据库表说明

#### Camunda核心表（自动创建）

| 表前缀 | 用途 |
|--------|------|
| ACT_RE_* | 流程定义表（Repository） |
| ACT_RU_* | 运行时表（Runtime） |
| ACT_HI_* | 历史表（History） |
| ACT_ID_* | 身份表（Identity） |

#### 业务扩展表

| 表名 | 用途 |
|------|------|
| wf_process_instance | 流程实例扩展信息 |
| wf_task | 任务扩展信息 |
| wf_sla_config | SLA配置 |
| wf_sla_monitor | SLA监控记录 |
| wf_working_calendar | 工作日历 |
| wf_working_calendar_detail | 工作日历详情 |
| wf_variable_history | 流程变量变更历史 |

---

## 监控配置

### Prometheus指标采集

#### Workflow Engine Service指标

```yaml
# Spring Boot Actuator暴露的指标
- jvm_memory_used_bytes
- jvm_gc_pause_seconds
- http_server_requests_seconds
- camunda_process_instances_created_total
- camunda_process_instances_completed_total
- camunda_tasks_created_total
- camunda_tasks_completed_total
- camunda_sla_compliant_rate
```

#### 自定义指标（需要在代码中实现）

```java
@Component
public class CamundaMetricsExporter {

    private final MeterRegistry meterRegistry;

    public void exportProcessInstanceMetrics() {
        // 流程实例指标
        Gauge.builder("camunda.process.instances.running", this::getRunningProcessCount)
            .register(meterRegistry);

        // 任务指标
        Gauge.builder("camunda.tasks.pending", this::getPendingTaskCount)
            .register(meterRegistry);

        // SLA指标
        Gauge.builder("camunda.sla.compliant.rate", this::getSLACompliantRate)
            .register(meterRegistry);
    }
}
```

### Grafana仪表板

导入仪表板JSON文件：

1. 登录Grafana (http://localhost:3000)
2. 导航到 Dashboards -> Import
3. 上传 `camunda-dashboard.json`

仪表板包含以下面板：

- 流程实例统计
- 任务统计
- SLA响应达成率
- SLA解决达成率
- 各流程定义启动次数

### SLA告警规则

告警规则文件：`prometheus/sla-alert-rules.yml`

| 告警项 | 触发条件 | 级别 |
|--------|----------|------|
| 流程执行时间过长 | 执行时间 > 1小时 | WARNING |
| 待处理任务积压 | 积压数量 > 500 | WARNING |
| SLA响应达成率过低 | 达成率 < 90% | WARNING |
| SLA解决达成率过低 | 达成率 < 85% | WARNING |
| SLA超时数量过多 | 超时数量 > 100 | CRITICAL |
| 高优先级工单SLA超时 | 任何高优先级超时 | CRITICAL |

---

## 开发环境使用

### 1. 部署BPMN流程

方式一：通过Camunda Modeler

1. 在Camunda Modeler中创建流程
2. 点击"Deploy"按钮
3. 输入Camunda REST API地址: http://localhost:8080/camunda/engine-rest
4. 点击"Deploy"

方式二：通过REST API

```bash
curl -X POST "http://localhost:8080/camunda/engine-rest/deployment/create" \
  -H "Content-Type: multipart/form-data" \
  -F "deployment-name=property-repair-workorder" \
  -F "tenant-id=DEFAULT" \
  -F "upload=@property-repair-workorder.bpmn"
```

### 2. 启动流程实例

```bash
curl -X POST "http://localhost:8080/camunda/engine-rest/process-definition/key/property-repair-workorder/start" \
  -H "Content-Type: application/json" \
  -d '{
    "businessKey": "WO-2026-000001",
    "variables": {
      "workOrderType": {"value": "REPAIR", "type": "String"},
      "spaceId": {"value": "space-001", "type": "String"},
      "priority": {"value": "HIGH", "type": "String"}
    }
  }'
```

### 3. 查询待办任务

```bash
curl -X GET "http://localhost:8080/camunda/engine-rest/task?assignee=admin"
```

### 4. 完成任务

```bash
curl -X POST "http://localhost:8080/camunda/engine-rest/task/{taskId}/complete" \
  -H "Content-Type: application/json" \
  -d '{
    "variables": {
      "result": {"value": "COMPLETED", "type": "String"}
    }
  }'
```

---

## 故障排查

### Camunda启动失败

```bash
# 查看Camunda日志
docker logs camunda-platform --tail 100

# 检查数据库连接
docker exec camunda-postgres pg_isready -U camunda

# 查看Camunda核心表是否创建
docker exec camunda-postgres psql -U camunda -d camunda -c "\dt ACT_*"
```

### Workflow Engine Service连接失败

```bash
# 检查网络连通性
docker exec workflow-engine-service ping camunda

# 检查Camunda API可用性
docker exec workflow-engine-service wget -O- http://camunda:8080/camunda/engine-rest

# 查看应用日志
docker logs workflow-engine-service --tail 100
```

### 数据库连接问题

```bash
# 检查PostgreSQL日志
docker logs camunda-postgres --tail 100

# 验证数据库凭据
docker exec camunda-postgres psql -U camunda -d camunda -c "SELECT 1"
```

---

## 目录结构

```
devops/camunda/
├── docker-compose.yml         # Docker Compose配置
├── Dockerfile.workflow-engine # Workflow Engine Service Dockerfile
├── postgres/
│   └── init/
│       └── 01-camunda-init.sql  # 数据库初始化脚本
├── grafana/
│   └── camunda-dashboard.json   # Grafana仪表板
├── prometheus/
│   ├── prometheus.yml          # Prometheus配置
│   └── sla-alert-rules.yml     # SLA告警规则
└── README.md                   # 本文档
```

---

## 参考文档

- [Camunda官方文档](https://docs.camunda.org/)
- [Camunda Spring Boot Starter](https://docs.camunda.org/manuals/7.20/spring-boot-integration/)
- [工作流引擎集成方案设计](D:/ai-agentic/docs/design/工作流引擎集成方案设计.md)
