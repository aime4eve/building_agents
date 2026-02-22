# AGENTS.md - 华宽通智能体平台开发指南

本文档为AI编码代理提供项目上下文，确保生成的代码符合项目规范。

---

## 项目概述

华宽通智能体平台是基于DDD（领域驱动设计）架构的物联网智能管理平台，包含：

- **hkt-iot-web**: Vue 3 + TypeScript 前端应用
- **hkt-iot-platform**: Spring Cloud 微服务后端

---

## 构建与测试命令

### 前端 (hkt-iot-web)

```bash
# 进入前端目录
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

# 预览构建结果
npm run preview
```

### 后端 (hkt-iot-platform)

```bash
# 进入后端目录
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

# 运行单个测试方法
mvn test -Dtest=DeviceApplicationServiceTest#testCreateDevice

# 启动单个服务 (需要先启动 Nacos、MySQL、Redis)
java -jar hkt-iot-device-service/target/hkt-iot-device-service-1.0.0-SNAPSHOT.jar
```

---

## 前端代码规范 (hkt-iot-web)

### 目录结构

```
src/
├── api/          # API接口定义，按领域模块划分
├── assets/       # 静态资源
├── components/   # 公共组件
│   ├── business/ # 业务组件
│   └── common/   # 通用组件
├── directives/   # 自定义指令
├── router/       # 路由配置
├── stores/       # Pinia状态管理
├── types/        # TypeScript类型定义
├── utils/        # 工具函数
└── views/        # 页面组件
```

### 导入规范

```typescript
// 1. Vue 相关
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 2. 第三方库
import { message } from 'ant-design-vue'
import type { TableProps } from 'ant-design-vue'

// 3. 类型导入 (使用 type 关键字)
import type { Device, DeviceQuery } from '@/types'

// 4. 本地模块 (使用 @ 别名)
import { deviceApi } from '@/api/device'
import { useUserStore } from '@/stores/user'

// 5. 图标 (按需导入)
import {
  SearchOutlined,
  PlusOutlined,
} from '@ant-design/icons-vue'
```

### 组件规范

使用 Vue 3 Composition API + `<script setup>`:

```vue
<template>
  <div class="container">
    <!-- 模板内容 -->
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

// Props 定义
interface Props {
  deviceId: string
}
const props = defineProps<Props>()

// Emits 定义
const emit = defineEmits<{
  (e: 'update', value: string): void
}>()

// 响应式状态
const loading = ref(false)
const device = ref<Device | null>(null)

// 方法
const fetchData = async () => {
  loading.value = true
  // ...
}

// 生命周期
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.container {
  padding: 24px;
}
</style>
```

### API 定义规范

API 按领域模块组织，统一返回类型:

```typescript
// src/api/device.ts
import { http } from '@/utils/request'
import type { ApiResponse, PageResult, PageRequest } from '@/types'

// 类型定义在前
export interface Device {
  deviceId: string
  deviceName: string
  // ...
}

export interface DeviceQuery extends PageRequest {
  tenantId?: string
  deviceType?: DeviceType
}

// API 对象导出
export const deviceApi = {
  getDevices(params: DeviceQuery): Promise<ApiResponse<PageResult<Device>>> {
    return http.get('/v1/devices', { params })
  },

  getDevice(deviceId: string): Promise<ApiResponse<Device>> {
    return http.get(`/v1/devices/${deviceId}`)
  },
}
```

### Store 规范 (Pinia)

使用 Composition API 风格:

```typescript
// src/stores/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>('')
  const userInfo = ref<User | null>(null)
  const loading = ref(false)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)

  // 方法
  const login = async (credentials: LoginRequest) => {
    loading.value = true
    try {
      // ...
    } finally {
      loading.value = false
    }
  }

  return {
    token,
    userInfo,
    loading,
    isLoggedIn,
    login,
  }
})
```

### 类型定义规范

```typescript
// src/types/index.ts

// 分页请求
export interface PageRequest {
  page: number
  size: number
  sortBy?: string
  direction?: 'ASC' | 'DESC'
}

// 分页响应
export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

// API 响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 枚举类型使用 type 联合
export type DeviceStatus = 'ONLINE' | 'OFFLINE' | 'FAULT' | 'LOCKED'
export type DeviceType = 'WATER_METER' | 'ELECTRIC_METER' | 'GAS_METER' | ...
```

---

## 后端代码规范 (hkt-iot-platform)

### DDD 分层架构

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
    ├── rest/                  # REST控制器
    └── dto/                   # 请求/响应DTO
```

### 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 聚合根 | 业务名称，无后缀 | `Device`, `Tenant`, `Order` |
| 实体 | 业务名称 | `ThingModel`, `Property` |
| 值对象 | 属性名称 | `DeviceId`, `DeviceSn`, `Money` |
| 仓储接口 | `{Aggregate}Repository` | `DeviceRepository` |
| 领域服务 | `{Aggregate}DomainService` | `DeviceDomainService` |
| 应用服务 | `{Aggregate}ApplicationService` | `DeviceApplicationService` |
| 控制器 | `{Aggregate}Controller` | `DeviceController` |
| 领域事件 | `{Aggregate}{PastTense}Event` | `DeviceActivatedEvent` |

### 聚合根示例

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Device {
    // 聚合根ID
    private DeviceId id;
    private Long version;  // 乐观锁
    
    // 业务属性
    private DeviceSn sn;
    private DeviceName name;
    private DeviceStatus status;
    
    // 领域事件集合
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    // 工厂方法
    public static Device create(DeviceSn sn, DeviceName name, TenantId tenantId) {
        Device device = new Device();
        // 初始化...
        device.registerDomainEvent(new DeviceCreatedEvent(device.id, ...));
        return device;
    }
    
    // 业务方法 - 封装业务规则
    public void activate() {
        if (this.status == DeviceStatus.ACTIVE) return; // 幂等性
        this.status = DeviceStatus.ACTIVE;
        this.activatedAt = LocalDateTime.now();
        registerDomainEvent(new DeviceActivatedEvent(this.id, ...));
    }
    
    // 领域事件管理
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

### 值对象示例

```java
@Value  // Lombok 自动生成不可变类
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

### 禁止事项

- **禁止** 使用 `as any`、`@ts-ignore`、`@ts-expect-error`
- **禁止** 空的 catch 块
- **禁止** 聚合根直接引用其他聚合根对象（只引用ID）
- **禁止** 业务逻辑泄露到应用层或接口层
- **禁止** 删除测试以使其通过

---

## 错误处理

### 前端

```typescript
// HTTP 请求已在 utils/request.ts 统一处理
// 业务错误通过 message.error() 提示
// 特殊状态码（401）自动跳转登录

try {
  const response = await deviceApi.getDevices(params)
  // 处理成功响应
} catch (error) {
  // 错误已在拦截器中处理，此处可做额外处理
  console.error('Failed:', error)
}
```

### 后端

```java
// 领域层：抛出领域异常
if (device == null) {
    throw new DeviceNotFoundException(deviceId);
}

// 应用层：捕获并转换异常
@Transactional
public DeviceId createDevice(CreateDeviceCommand command) {
    try {
        Device device = deviceDomainService.createDevice(...);
        deviceRepository.save(device);
        return device.getId();
    } catch (DuplicateDeviceSnException e) {
        throw new BusinessException("设备序列号已存在", e);
    }
}

// 接口层：全局异常处理
@ExceptionHandler(DeviceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(DeviceNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(404, e.getMessage()));
}
```

---

## 提交前检查清单

- [ ] TypeScript 编译无错误
- [ ] ESLint 检查通过
- [ ] 后端 Maven 构建成功
- [ ] 相关测试通过
- [ ] 遵循 DDD 分层架构
- [ ] 聚合根封装业务规则
- [ ] 值对象不可变
- [ ] 无类型安全绕过

---

**文档版本**: V1.0  
**更新日期**: 2026-02-21
