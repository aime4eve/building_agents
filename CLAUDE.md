# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

华宽通智能体平台是基于DDD（领域驱动设计）架构的物联网智能管理平台，包含以下主要子项目：

- **hkt-iot-web/**: Vue 3 + TypeScript 前端应用（Ant Design Vue）
- **hkt-iot-platform/**: Spring Cloud 微服务后端平台
- **huakuangtong-agent/**: Python智能体核心
- **devops/**: Docker、Kubernetes、CI/CD配置

---

## 常用命令

### 前端开发 (hkt-iot-web)

```bash
cd hkt-iot-web

# 安装依赖
npm install

# 开发服务器 (端口3000)
npm run dev

# 类型检查 + 构建
npm run build

# 代码检查
npm run lint

# 代码检查 + 自动修复
npm run lint:fix
```

### 后端开发 (hkt-iot-platform)

```bash
cd hkt-iot-platform

# 编译所有模块 (跳过测试)
mvn clean package -DskipTests

# 编译并运行测试
mvn clean verify

# 运行单个服务的测试
cd hkt-iot-device-service
mvn test

# 运行单个测试类
mvn test -Dtest=DeviceApplicationServiceTest

# 启动单个服务 (需先启动基础设施)
java -jar target/hkt-iot-device-service-1.0.0-SNAPSHOT.jar
```

### DevOps本地环境

```bash
cd devops/docker

# 启动所有基础设施 (MySQL, Redis, RabbitMQ, EMQX, Nacos等)
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止并删除数据卷
docker-compose down -v
```

**服务访问地址：**
- MySQL: localhost:3306 (root/root123456)
- Redis: localhost:6379
- RabbitMQ: http://localhost:15672 (admin/admin123456)
- EMQX: http://localhost:18083 (admin/admin123456)
- Nacos: http://localhost:8848/nacos (nacos/nacos)
- Grafana: http://localhost:3000 (admin/admin123456)
- Prometheus: http://localhost:9090

---

## 架构概述

### DDD分层架构

后端采用严格的DDD四层架构：

```
src/main/java/com/hkt/iot/{service}/
├── domain/                    # 领域层（核心业务逻辑）
│   ├── model/
│   │   ├── aggregate/         # 聚合根 - 业务名称，无后缀
│   │   ├── entity/            # 实体 - 业务名称
│   │   ├── valueobject/       # 值对象 - 属性名称 (如DeviceId, Money)
│   │   └── domainevent/       # 领域事件 - {Aggregate}{PastTense}Event
│   ├── service/               # 领域服务 - {Aggregate}DomainService
│   └── repository/            # 仓储接口 - {Aggregate}Repository
├── application/               # 应用层（用例编排）
│   ├── service/               # 应用服务 - {Aggregate}ApplicationService
│   ├── command/               # 命令对象 - Create{Aggregate}Command
│   ├── query/                 # 查询对象 - {Aggregate}Query
│   └── dto/                   # 数据传输对象
├── infrastructure/            # 基础设施层
│   ├── persistence/           # 仓储实现 - {Aggregate}RepositoryImpl
│   ├── messaging/             # 消息发布
│   └── config/                # 配置
└── interfaces/                # 接口层
    ├── rest/                  # REST控制器 - {Aggregate}Controller
    └── dto/                   # 请求/响应DTO
```

### 微服务模块

| 服务 | 限界上下文 | 端口 | 实现程度 |
|------|-----------|------|----------|
| hkt-iot-user-service | 用户与租户管理 | 8081 | 85% |
| hkt-iot-device-service | 设备管理 | 8082 | 85% |
| hkt-iot-space-service | 空间管理 | 8083 | 80% |
| hkt-iot-rule-service | 规则引擎 | 8084 | 90% |
| hkt-iot-scene-service | 场景联动 | - | 90% |
| hkt-iot-notification-service | 通知中心 | - | 85% |
| hkt-iot-order-service | 订单中心 | - | 0% (待开发) |
| hkt-iot-smart-apps-service | 防霉管控/智慧畜牧 | - | 30% |
| hkt-iot-workflow-service | 工作流引擎 | - | 0% |
| hkt-iot-device-ingestion-service | 设备数据接入 | - | 60% |
| hkt-iot-gateway | API网关 | 8080 | 50% |

### 技术栈

| 类别 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.5, Spring Cloud 2023.0.1, Spring Cloud Alibaba 2023.0.1.0 |
| 服务治理 | Nacos 2.3.x |
| 消息队列 | RabbitMQ 3.12+, Kafka |
| 数据库 | MySQL 8.0+, Redis 7.2+, InfluxDB |
| 设备协议 | MQTT (EMQX 5.x) |
| 工作流 | Camunda 8 |
| 前端 | Vue 3.5, TypeScript 5.9, Ant Design Vue 4.2, Vite 7 |
| 容器 | Docker, Kubernetes 1.28+ |

---

## 开发规范

### 命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| 聚合根 | 业务名称，无后缀 | `Device`, `Tenant`, `Order`, `MoldPreventionZone` |
| 实体 | 业务名称 | `ThingModel`, `Property`, `OrderItem` |
| 值对象 | 属性名称 | `DeviceId`, `DeviceSn`, `Money`, `Address` |
| ID值对象 | {实体}Id | `DeviceId`, `TenantId`, `OrderId` |
| 仓储接口 | {聚合根}Repository | `DeviceRepository`, `TenantRepository` |
| 领域服务 | {聚合根}DomainService | `DeviceDomainService` |
| 应用服务 | {聚合根}ApplicationService | `DeviceApplicationService` |
| 控制器 | {聚合根}Controller | `DeviceController` |
| 领域事件 | {聚合根}{过去分词}Event | `DeviceActivatedEvent`, `OrderCreatedEvent` |

### 前端规范 (Vue 3 + TypeScript)

使用 Composition API + `<script setup>`:

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'

interface Props {
  deviceId: string
}
const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update', value: string): void
}>()

const loading = ref(false)
const device = ref<Device | null>(null)
</script>
```

导入顺序：
1. Vue 相关 (`vue`, `vue-router`)
2. 第三方库 (`ant-design-vue`)
3. 类型导入 (使用 `type` 关键字)
4. 本地模块 (使用 `@` 别名)
5. 图标 (按需导入)

### 后端聚合根规范

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Device {
    // 聚合根ID - 值对象
    private DeviceId id;
    private Long version;  // 乐观锁版本号

    // 业务属性 - 使用值对象
    private DeviceSn sn;
    private DeviceName name;
    private DeviceStatus status;

    // 关联 - 只引用ID
    private SpaceId spaceId;
    private TenantId tenantId;

    // 领域事件集合
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // 工厂方法创建
    public static Device create(DeviceSn sn, DeviceName name, TenantId tenantId) {
        Device device = new Device();
        // 初始化和业务规则验证
        device.registerDomainEvent(new DeviceCreatedEvent(...));
        return device;
    }

    // 业务方法 - 封装业务规则和不变量保护
    public void activate() {
        if (this.status == DeviceStatus.ACTIVE) return; // 幂等性
        this.status = DeviceStatus.ACTIVE;
        registerDomainEvent(new DeviceActivatedEvent(...));
    }

    protected void registerDomainEvent(DomainEvent event) {
        this.domainEvents.add(Objects.requireNonNull(event));
    }
}
```

### 值对象规范

```java
@Value  // Lombok @Value 确保不可变
public class DeviceSn {
    String value;

    private DeviceSn(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("DeviceSn cannot be empty");
        }
        if (!value.matches("^[A-Z0-9]{12,20}$")) {
            throw new IllegalArgumentException("Invalid DeviceSn format");
        }
        this.value = value.toUpperCase();
    }

    public static DeviceSn of(String value) {
        return new DeviceSn(value);
    }
}
```

---

## 关键设计原则

### 聚合设计原则
- **小聚合**: 聚合尽量小，避免性能问题
- **不变性保护**: 通过构造函数和业务方法保护不变量
- **最终一致性**: 跨聚合通过事件实现最终一致性
- **ID引用**: 跨聚合只引用ID，不引用对象
- **乐观锁**: 使用版本号实现并发控制

### 禁止事项
- 禁止聚合根直接引用其他聚合根对象（只引用ID）
- 禁止业务逻辑泄露到应用层或接口层
- 禁止使用 `as any`、`@ts-ignore`、`@ts-expect-error`
- 禁止空的 catch 块
- 禁止删除测试以使其通过

---

## 重要文档参考

- `AGENTS.md` - AI编码代理开发指南（前端/后端详细规范）
- `DDD开发规范指南.md` - 完整的DDD实现规范（命名、事件、分层、最佳实践）
- `DDD代码评审清单.md` - 代码评审检查清单
- `开发计划.md` - 项目里程碑和任务分解
- `hkt-iot-platform/README.md` - 后端平台文档
- `devops/README.md` - DevOps环境搭建指南

---

## 项目状态

当前实现率约36%（175/480类）：

| 里程碑 | 阶段 | 状态 |
|--------|------|------|
| M0 | Phase 1收尾 | 进行中 - 通知事件契约、设备权限校验 |
| M1 | 订单中心 | 未开始 - 0% |
| M2 | 智能应用 | 部分完成 - 防霉30%，智慧畜牧后端待开发 |
| M3 | 订阅与审计 | 未开始 |
| M4 | 支撑平台 | 未开始 - 工作流引擎0% |
| M5 | 智慧园区 | Phase 3规划 |
| M6 | 进阶功能 | 远期规划 |
