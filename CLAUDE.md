# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 项目概述

华宽通智能体平台是基于 DDD（领域驱动设计）架构的物联网智能管理平台。

**项目结构:**
- `hkt-iot-web/` - Vue 3 + TypeScript 前端应用
- `hkt-iot-platform/` - Spring Cloud 微服务后端

---

## 构建与测试命令

### 前端 (hkt-iot-web)

```bash
cd hkt-iot-web

# 安装依赖
npm install

# 开发服务器 (端口 3000)
npm run dev

# 类型检查 + 构建
npm run build

# 代码检查
npm run lint

# 代码检查 + 自动修复
npm run lint:fix
```

### 后端 (hkt-iot-platform)

```bash
cd hkt-iot-platform

# 编译所有模块 (跳过测试)
mvn clean package -DskipTests

# 编译并运行测试
mvn clean verify

# 运行单个服务的测试
cd hkt-iot-device-service && mvn test

# 运行单个测试类
mvn test -Dtest=DeviceApplicationServiceTest

# 运行单个测试方法
mvn test -Dtest=DeviceApplicationServiceTest#testCreateDevice
```

---

## 架构总览

### 后端微服务

| 服务 | 端口 | 限界上下文 |
|------|------|------------|
| hkt-iot-user-service | 8081 | 用户与租户管理 (BC-01) |
| hkt-iot-device-service | 8082 | 设备管理 (BC-04) |
| hkt-iot-space-service | 8083 | 空间管理 (BC-03) |
| hkt-iot-rule-service | 8084 | 规则引擎 (BC-05) |
| hkt-iot-scene-service | - | 场景联动与定时计划 |
| hkt-iot-notification-service | - | 通知中心 |
| hkt-iot-smart-apps-service | - | 防霉管控/智慧畜牧 |
| hkt-iot-order-service | - | 订单与交易中心 |
| hkt-iot-gateway | 8080 | API 网关 |

### 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| Java | JDK | 17 |
| Spring Boot | | 3.2.5 |
| Spring Cloud | | 2023.0.1 |
| Spring Cloud Alibaba | | 2023.0.1.0 |
| Nacos | 服务注册/配置中心 | 2.3.x |
| MySQL | 数据库 | 8.0+ |
| Redis | 缓存 | 7.2+ |
| RabbitMQ | 消息队列 | 3.12+ |
| MQTT | 设备接入 | EMQX 5.x |
| MyBatis Plus | ORM | 3.5.5 |

---

## DDD 分层架构

```
src/main/java/com/hkt/iot/{service}/
├── domain/                    # 领域层（核心业务逻辑）
│   ├── model/
│   │   ├── aggregate/         # 聚合根
│   │   ├── entity/            # 实体
│   │   ├── valueobject/       # 值对象
│   │   └── domainevent/       # 领域事件
│   ├── service/               # 领域服务
│   └── repository/            # 仓储接口
├── application/               # 应用层（用例编排）
│   ├── service/               # 应用服务
│   ├── command/               # 命令对象
│   ├── query/                 # 查询对象
│   └── dto/                   # 数据传输对象
├── infrastructure/            # 基础设施层
│   ├── persistence/           # 仓储实现
│   ├── messaging/             # 消息发布
│   └── config/                # 配置
└── interfaces/                # 接口层
    ├── rest/                  # REST 控制器
    └── dto/                   # 请求/响应 DTO
```

### 依赖方向

```
interfaces → application → domain ← infrastructure
```

**关键规则:**
- 领域层必须是纯 Java 代码，无 Spring 注解
- 仓储接口定义在 `domain/`，实现在 `infrastructure/persistence/`
- 业务逻辑只能存在于领域层

---

## 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 聚合根 | 业务名称，无后缀 | `Device`, `Tenant`, `Order` |
| 实体 | 业务名称 | `ThingModel`, `OrderItem` |
| 值对象 | 属性名称 | `DeviceId`, `DeviceSn`, `Money` |
| 仓储接口 | `{Aggregate}Repository` | `DeviceRepository` |
| 领域服务 | `{Aggregate}DomainService` | `DeviceDomainService` |
| 应用服务 | `{Aggregate}ApplicationService` | `DeviceApplicationService` |
| 控制器 | `{Aggregate}Controller` | `DeviceController` |
| 领域事件 | `{Aggregate}{PastTense}Event` | `DeviceActivatedEvent` |

---

## 代码示例

### 聚合根

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Device {
    private DeviceId id;
    private Long version;  // 乐观锁

    private DeviceSn sn;
    private DeviceName name;
    private DeviceStatus status;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public static Device create(DeviceSn sn, DeviceName name, TenantId tenantId) {
        Device device = new Device();
        device.registerDomainEvent(new DeviceCreatedEvent(device.id, ...));
        return device;
    }

    public void activate() {
        if (this.status == DeviceStatus.ACTIVE) return;
        this.status = DeviceStatus.ACTIVE;
        registerDomainEvent(new DeviceActivatedEvent(this.id, ...));
    }

    protected void registerDomainEvent(DomainEvent event) {
        this.domainEvents.add(Objects.requireNonNull(event));
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
```

### 值对象

```java
@Value
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

## 前端规范 (hkt-iot-web)

### 目录结构

```
src/
├── api/          # API 接口
├── assets/       # 静态资源
│   └── styles/   # 全局样式
├── components/   # 组件
│   ├── business/ # 业务组件 (DataTable, DeviceStatusCard, TelemetryChart)
│   └── common/   # 通用组件 (Layout)
├── stores/       # Pinia 状态管理
├── types/        # TypeScript 类型
└── views/        # 页面组件
```

### 设计令牌系统

项目使用 CSS 变量统一管理设计令牌：

```css
:root {
  /* 品牌色 */
  --hkt-primary: #1890ff;
  --hkt-primary-hover: #40a9ff;
  --hkt-primary-active: #096dd9;

  /* 渐变色 */
  --hkt-gradient-brand: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  /* 阴影层级 */
  --hkt-shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.03);
  --hkt-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  --hkt-shadow-md: 0 4px 16px rgba(0, 0, 0, 0.12);
  --hkt-shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.15);

  /* 圆角规范 */
  --hkt-radius-xs: 2px;
  --hkt-radius-sm: 4px;
  --hkt-radius-md: 8px;
  --hkt-radius-lg: 12px;
  --hkt-radius-xl: 16px;
}
```

### 组件模板

```vue
<template>
  <div class="container">
    <!-- 模板内容 -->
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { deviceApi } from '@/api/device'
import type { Device } from '@/types'

const loading = ref(false)
const device = ref<Device | null>(null)

const fetchData = async () => {
  loading.value = true
  // ...
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="less">
@primary-color: #1890ff;
@border-radius: 12px;
@transition-base: all 0.3s ease;

.container {
  padding: 24px;
  border-radius: @border-radius;
  transition: @transition-base;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }
}
</style>
```

### UI/UX 设计原则

- **一致性**: 统一使用圆角分级 (2px→24px)、5 级阴影系统
- **层次感**: 通过阴影和上浮效果建立视觉层级
- **反馈性**: 所有交互都有悬停反馈 (上浮 + 阴影增强)
- **流畅性**: 使用缓动曲线 `all 0.3s ease` 优化动画
- **响应式**: 支持移动端自适应和暗色模式

---

## 禁止事项

- **禁止** 使用 `as any`、`@ts-ignore`、`@ts-expect-error`
- **禁止** 空的 catch 块
- **禁止** 聚合根直接引用其他聚合根对象（只引用 ID）
- **禁止** 业务逻辑泄露到应用层或接口层
- **禁止** 领域层依赖 Spring 或其他框架

---

## 基础设施依赖

启动服务前需要:
- Nacos 2.3+ (服务注册/配置中心) - http://localhost:8848/nacos
- MySQL 8.0+ (数据库) - 端口 3306
- Redis 7.0+ (缓存) - 端口 6379
- RabbitMQ 3.12+ (消息队列) - 端口 5672
- EMQX 5.x (可选，MQTT 设备接入) - 端口 1883

---

## API 文档

- API 网关：http://localhost:8080
- Knife4j 文档：http://localhost:8080/doc.html

---

## 相关文档

详细开发规范请参考:
- `DDD 开发规范指南.md` - DDD 代码规范详解
- `DDD 代码评审清单.md` - 代码评审检查项
- `AGENTS.md` - 前端/后端开发指南
