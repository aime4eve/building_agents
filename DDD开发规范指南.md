# DDD开发规范指南

**文档版本：** V1.0
**创建日期：** 2026-02-20
**适用项目：** 华宽通智能体系统
**设计方法：** 领域驱动设计（Domain-Driven Design）

---

## 目录

1. [代码结构规范](#1-代码结构规范)
2. [命名规范](#2-命名规范)
3. [领域事件规范](#3-领域事件规范)
4. [代码示例](#4-代码示例)
5. [最佳实践](#5-最佳实践)
6. [反模式识别](#6-反模式识别)

---

## 1. 代码结构规范

### 1.1 整体分层架构

```
src/main/java/com/huaquantomt/
├── domain/                    # 领域层（核心业务逻辑）
│   ├── model/                # 领域模型
│   │   ├── aggregate/        # 聚合根
│   │   ├── entity/           # 实体
│   │   ├── valueobject/      # 值对象
│   │   └── domainevent/      # 领域事件
│   ├── service/              # 领域服务
│   └── repository/           # 仓储接口（端口）
├── application/              # 应用层（用例编排）
│   ├── service/              # 应用服务
│   ├── command/              # 命令对象
│   ├── query/                # 查询对象
│   └── dto/                  # 数据传输对象
├── infrastructure/           # 基础设施层（技术实现）
│   ├── persistence/          # 持久化实现
│   ├── messaging/            # 消息发布
│   ├── external/             # 外部服务适配器
│   └── config/               # 配置
└── interfaces/               # 接口层（对外暴露）
    ├── rest/                 # REST控制器
    ├── dto/                  # 请求/响应DTO
    └── assembler/            # DTO转换器
```

### 1.2 domain层规范

#### 1.2.1 聚合根（Aggregate Root）

**职责：**
- 维护聚合内部一致性边界
- 封装业务规则和不变性约束
- 发布领域事件

**规范要求：**
```java
// 位置：domain/model/aggregate/
public class Device {

    // 1. 聚合根ID必须是唯一标识
    private final DeviceId id;

    // 2. 版本号用于乐观锁
    private Long version;

    // 3. 领域事件集合
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // 4. 构造函数私有化，通过工厂方法或仓储重建
    private Device(DeviceId id, DeviceSn sn, DeviceName name, ...) {
        this.id = Objects.requireNonNull(id, "DeviceId cannot be null");
        this.sn = Objects.requireNonNull(sn, "DeviceSn cannot be null");
        // ... 其他字段初始化和业务规则验证
    }

    // 5. 业务方法必须是public，返回void或值对象
    public void activate() {
        if (this.status == DeviceStatus.ACTIVE) {
            return; // 幂等性
        }
        this.status = DeviceStatus.ACTIVE;
        this.activatedAt = DateTime.now();
        registerDomainEvent(new DeviceActivatedEvent(this.id, DateTime.now()));
    }

    // 6. 领域事件管理
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

#### 1.2.2 实体（Entity）

**规范要求：**
```java
// 位置：domain/model/entity/
public class ThingModel {

    // 1. 实体必须有ID
    private ThingModelId id;

    // 2. 实体可以包含值对象集合
    private List<Property> properties;

    // 3. 实体方法修改内部状态
    public void addProperty(Property property) {
        Objects.requireNonNull(property);
        if (hasProperty(property.getIdentifier())) {
            throw new DuplicatePropertyException(property.getIdentifier());
        }
        this.properties.add(property);
    }

    // 4. 实体不发布领域事件（由聚合根负责）
}
```

#### 1.2.3 值对象（Value Object）

**规范要求：**
```java
// 位置：domain/model/valueobject/

// 方式一：使用Lombok @Value（推荐）
@Value
public class DeviceSn {
    String value;

    private DeviceSn(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("DeviceSn cannot be empty");
        }
        // 业务规则验证
        if (!value.matches("^[A-Z0-9]{12,20}$")) {
            throw new IllegalArgumentException("Invalid DeviceSn format");
        }
        this.value = value.toUpperCase();
    }

    public static DeviceSn of(String value) {
        return new DeviceSn(value);
    }
}

// 方式二：手动实现不可变类
public final class DeviceName {
    private final String value;

    private DeviceName(String value) {
        // 验证逻辑
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceName)) return false;
        DeviceName that = (DeviceName) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
```

### 1.3 application层规范

#### 1.3.1 应用服务（Application Service）

**职责：**
- 编排用例流程
- 调用领域服务
- 处理事务边界
- 不包含业务逻辑

**规范要求：**
```java
// 位置：application/service/
@Service
@Transactional
@RequiredArgsConstructor
public class DeviceApplicationService {

    private final DeviceRepository deviceRepository;
    private final DeviceDomainService deviceDomainService;
    private final AuditLogService auditLogService;

    // 1. 方法命名：动词 + 名词
    public DeviceId createDevice(CreateDeviceCommand command) {
        // 2. 参数校验
        validateCommand(command);

        // 3. 调用领域服务
        Device device = deviceDomainService.createDevice(
            command.getSn(),
            command.getName(),
            command.getType()
        );

        // 4. 持久化
        deviceRepository.save(device);

        // 5. 发布领域事件（由基础设施层处理）

        // 6. 返回结果
        return device.getId();
    }

    // 7. 查询方法在单独的查询服务中
    public DeviceDTO getDevice(DeviceId id) {
        Device device = deviceRepository.findById(id)
            .orElseThrow(() -> new DeviceNotFoundException(id));
        return DeviceAssembler.toDTO(device);
    }
}
```

#### 1.3.2 命令对象（Command）

**规范要求：**
```java
// 位置：application/command/
@Value
public class CreateDeviceCommand {
    DeviceSn sn;
    DeviceName name;
    DeviceType type;
    SpaceId spaceId;
    TenantId tenantId;

    // 自定义校验方法
    public void validate() {
        if (sn == null) {
            throw new IllegalArgumentException("DeviceSn is required");
        }
    }
}
```

#### 1.3.3 查询对象（Query）

**规范要求：**
```java
// 位置：application/query/
@Value
public class DeviceQuery {
    DeviceId id;
    DeviceSn sn;
    DeviceType type;
    DeviceStatus status;
    SpaceId spaceId;
    TenantId tenantId;
    PageRequest pageRequest;
}
```

### 1.4 infrastructure层规范

#### 1.4.1 仓储实现（Repository Implementation）

**规范要求：**
```java
// 位置：infrastructure/persistence/jpa/
@Repository
@RequiredArgsConstructor
public class DeviceRepositoryImpl implements DeviceRepository {

    private final DeviceJpaRepository jpaRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    @Transactional
    public Device save(Device device) {
        DevicePO po = DevicePO.fromDomain(device);
        DevicePO saved = jpaRepository.save(po);
        // 发布领域事件
        device.getDomainEvents().forEach(eventPublisher::publish);
        device.clearDomainEvents();
        return saved.toDomain();
    }

    @Override
    public Optional<Device> findById(DeviceId id) {
        return jpaRepository.findById(id.getValue())
            .map(DevicePO::toDomain);
    }
}
```

### 1.5 interfaces层规范

#### 1.5.1 REST控制器

**规范要求：**
```java
// 位置：interfaces/rest/
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "Device", description = "设备管理API")
public class DeviceController {

    private final DeviceApplicationService deviceApplicationService;

    @PostMapping
    @Operation(summary = "创建设备")
    public ResponseEntity<DeviceResponse> createDevice(
        @Valid @RequestBody CreateDeviceRequest request
    ) {
        CreateDeviceCommand command = CreateDeviceCommand.from(request);
        DeviceId deviceId = deviceApplicationService.createDevice(command);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(DeviceResponse.of(deviceId));
    }
}
```

---

## 2. 命名规范

### 2.1 聚合根命名

| 规则 | 示例 | 说明 |
|------|------|------|
| 使用业务名称 | `Device`, `Tenant`, `Order` | 直接反映业务概念 |
| 无后缀 | `MoldPreventionZone` | 避免使用Aggregate后缀 |
| 复合名词大写驼峰 | `BatchDeviceControlTask` | 多个单词组合 |

### 2.2 实体命名

| 规则 | 示例 | 说明 |
|------|------|------|
| 使用业务名称 | `ThingModel`, `Property` | 直接反映业务概念 |
| 无Entity后缀 | ❌ `PropertyEntity` | 避免使用技术术语后缀 |

### 2.3 值对象命名

| 规则 | 示例 | 说明 |
|------|------|------|
| 使用@Value注解 | `@Value class DeviceSn` | Lombok自动生成不可变类 |
| 或手动实现不可变 | `final class DeviceName` | 包含value()方法 |
| ID值对象 | `DeviceId`, `TenantId` | 聚合根ID都是值对象 |
| 枚举类型 | `enum DeviceType` | 固定选项使用枚举 |

### 2.4 领域事件命名

**格式：** `[聚合根名称][过去分词动作]Event`

| 规则 | 示例 | 说明 |
|------|------|------|
| 创建 | `DeviceCreatedEvent` | 聚合根首次创建 |
| 状态变更 | `DeviceActivatedEvent` | 状态变为激活 |
| 属性变更 | `DeviceNameChangedEvent` | 属性值修改 |
| 关联建立 | `DeviceBoundToSpaceEvent` | 建立关联关系 |
| 关联解除 | `DeviceUnboundFromSpaceEvent` | 解除关联关系 |
| 删除 | `DeviceDeletedEvent` | 聚合根删除 |

### 2.5 仓储命名

**格式：** `[聚合根名称]Repository`

```java
interface DeviceRepository extends BaseRepository<Device, DeviceId> { }
interface TenantRepository extends BaseRepository<Tenant, TenantId> { }
interface OrderRepository extends BaseRepository<Order, OrderId> { }
```

**特殊仓储：**
- 查询仓储：`DeviceTelemetryReadRepository`
- 批量操作：`BatchDeviceControlTaskRepository`

### 2.6 领域服务命名

**格式：** `[聚合根名称]DomainService` 或 `[业务功能]Service`

```java
class DeviceDomainService { }      // 推荐
class DeviceControlService { }     // 特定功能服务
class DeviceActivationService { }  // 特定业务流程
```

### 2.7 应用服务命名

**格式：** `[聚合根名称]ApplicationService`

```java
class DeviceApplicationService { }
class TenantApplicationService { }
class MoldPreventionApplicationService { }
```

### 2.8 ID值对象命名

**格式：** `[实体名称]Id`

```java
@Value
class DeviceId { String value; }

@Value
class TenantId { String value; }
```

---

## 3. 领域事件规范

### 3.1 事件命名规范

**规则：** `[聚合根名称][过去分词动作]Event`

```java
// 正确示例
class DeviceCreatedEvent { }
class DeviceActivatedEvent { }
class DeviceTelemetryReportedEvent { }

// 错误示例
class DeviceCreateEvent {}        // 应该用过去分词
class DeviceEvent {}              // 太泛化
class OnDeviceCreated {}          // 不要使用On前缀
```

### 3.2 事件字段规范

**必需字段：**
```java
public interface DomainEvent {
    String getEventId();          // 事件唯一ID
    DateTime getOccurredAt();     // 事件发生时间
    String getAggregateId();      // 关联聚合根ID
    String getAggregateType();    // 聚合根类型
    String getTenantId();         // 租户ID（多租户场景）
}
```

**完整示例：**
```java
public class DeviceActivatedEvent implements DomainEvent {

    private final String eventId;
    private final DateTime occurredAt;
    private final DeviceId deviceId;        // aggregateId
    private final String aggregateType = "Device";
    private final TenantId tenantId;

    public DeviceActivatedEvent(DeviceId deviceId, TenantId tenantId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = DateTime.now();
        this.deviceId = Objects.requireNonNull(deviceId);
        this.tenantId = Objects.requireNonNull(tenantId);
    }

    // Getters...
}
```

### 3.3 事件发布时机

**原则：** 状态变更后立即发布，事务边界内完成

```java
public class Device {
    public void activate(TenantId tenantId) {
        if (this.status == DeviceStatus.ACTIVE) {
            return; // 幂等性，不重复发布
        }

        DeviceStatus previousStatus = this.status;
        this.status = DeviceStatus.ACTIVE;
        this.activatedAt = DateTime.now();

        // 注册领域事件
        registerDomainEvent(new DeviceActivatedEvent(
            this.id,
            tenantId,
            previousStatus
        ));
    }

    protected void registerDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
}
```

### 3.4 事件幂等性

**幂等键设计：**
```java
public class DeviceTelemetryReportedEvent {
    private final DeviceId deviceId;
    private final String propertyIdentifier;
    private final DateTime reportedAt;
    private final String idempotencyKey;  // 幂等键

    public DeviceTelemetryReportedEvent(DeviceId deviceId,
                                       String propertyIdentifier,
                                       DateTime reportedAt) {
        this.deviceId = deviceId;
        this.propertyIdentifier = propertyIdentifier;
        this.reportedAt = reportedAt;
        // 幂等键：设备ID + 属性 + 时间戳（秒级）
        this.idempotencyKey = String.format("%s-%s-%d",
            deviceId.getValue(),
            propertyIdentifier,
            reportedAt.getMillis() / 1000
        );
    }
}
```

### 3.5 事件契约规范

```java
public interface DomainEvent {
    String EVENT_TYPE = "domain.event";  // 事件类型前缀

    default String getEventType() {
        return this.getClass().getSimpleName();
    }

    default String getEventKey() {
        return String.format("%s.%s",
            EVENT_TYPE,
            getEventType().replace("Event", "").toLowerCase()
        );
    }
}
```

---

## 4. 代码示例

### 4.1 Device聚合根完整实现

```java
package com.huaquantomt.device.domain.model.aggregate;

import com.huaquantomt.device.domain.model.domainevent.*;
import com.huaquantomt.device.domain.model.entity.*;
import com.huaquantomt.device.domain.model.valueobject.*;
import com.huaquantomt.shared.domain.event.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 设备聚合根
 *
 * 职责：
 * 1. 维护设备状态一致性
 * 2. 封装设备业务规则
 * 3. 发布设备相关领域事件
 *
 * 不变约束：
 * - 序列号（sn）在租户内唯一
 * - 激活后才能绑定空间
 * - 维护模式下不能接收命令
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Device {

    // ============== 聚合根标识 ==============

    /**
     * 设备ID - 聚合根唯一标识
     */
    private DeviceId id;

    /**
     * 设备序列号 - 业务唯一标识
     */
    private DeviceSn sn;

    /**
     * 设备名称
     */
    private DeviceName name;

    // ============== 设备属性 ==============

    /**
     * 设备类型
     */
    private DeviceType type;

    /**
     * 设备型号
     */
    private DeviceModel model;

    /**
     * 物模型定义
     */
    private ThingModel thingModel;

    /**
     * 设备配置
     */
    private DeviceConfig config;

    // ============== 运行状态 ==============

    /**
     * 设备状态
     */
    private DeviceStatus status;

    /**
     * 设备状态快照
     */
    private DeviceState state;

    /**
     * 最新遥测数据快照（写侧仅保留最新）
     */
    private DeviceTelemetry latestTelemetry;

    /**
     * 最新事件快照（写侧仅保留最新）
     */
    private DeviceEvent latestEvent;

    // ============== 关联关系 ==============

    /**
     * 绑定空间ID
     */
    private SpaceId spaceId;

    /**
     * 所属租户ID
     */
    private TenantId tenantId;

    /**
     * License ID
     */
    private LicenseId licenseId;

    // ============== 时间戳 ==============

    /**
     * 激活时间
     */
    private LocalDateTime activatedAt;

    /**
     * 最后上线时间
     */
    private LocalDateTime lastOnlineAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // ============== 乐观锁版本 ==============

    /**
     * 版本号（乐观锁）
     */
    private Long version;

    // ============== 领域事件 ==============

    /**
     * 待发布的领域事件
     */
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // ============== 工厂方法 ==============

    /**
     * 创建新设备
     */
    public static Device create(DeviceSn sn,
                               DeviceName name,
                               DeviceType type,
                               DeviceModel model,
                               TenantId tenantId) {
        Device device = new Device();
        device.id = DeviceId.generate();
        device.sn = Objects.requireNonNull(sn, "DeviceSn cannot be null");
        device.name = Objects.requireNonNull(name, "DeviceName cannot be null");
        device.type = Objects.requireNonNull(type, "DeviceType cannot be null");
        device.model = Objects.requireNonNull(model, "DeviceModel cannot be null");
        device.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        device.status = DeviceStatus.INACTIVE;
        device.createdAt = LocalDateTime.now();
        device.updatedAt = LocalDateTime.now();
        device.version = 0L;

        // 注册设备创建事件
        device.registerDomainEvent(new DeviceCreatedEvent(
            device.id,
            device.sn,
            device.type,
            device.tenantId,
            device.createdAt
        ));

        return device;
    }

    // ============== 业务方法 ==============

    /**
     * 激活设备
     *
     * 业务规则：
     * - 只有未激活的设备可以激活
     * - 激活后记录激活时间
     */
    public void activate() {
        if (this.status == DeviceStatus.ACTIVE) {
            return; // 幂等性
        }

        DeviceStatus previousStatus = this.status;
        this.status = DeviceStatus.ACTIVE;
        this.activatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new DeviceActivatedEvent(
            this.id,
            this.tenantId,
            previousStatus,
            this.activatedAt
        ));
    }

    /**
     * 停用设备
     */
    public void deactivate() {
        if (this.status == DeviceStatus.INACTIVE) {
            return;
        }

        this.status = DeviceStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new DeviceDeactivatedEvent(
            this.id,
            this.tenantId,
            this.updatedAt
        ));
    }

    /**
     * 设备上线
     *
     * 触发条件：设备连接到平台
     */
    public void online() {
        if (this.status == DeviceStatus.ONLINE) {
            return;
        }

        DeviceStatus previousStatus = this.status;
        this.status = DeviceStatus.ONLINE;
        this.lastOnlineAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new DeviceOnlineEvent(
            this.id,
            this.tenantId,
            previousStatus,
            this.lastOnlineAt
        ));
    }

    /**
     * 设备离线
     */
    public void offline() {
        if (this.status == DeviceStatus.OFFLINE) {
            return;
        }

        DeviceStatus previousStatus = this.status;
        this.status = DeviceStatus.OFFLINE;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new DeviceOfflineEvent(
            this.id,
            this.tenantId,
            previousStatus,
            this.updatedAt
        ));
    }

    /**
     * 设备故障
     *
     * @param reason 故障原因
     */
    public void fault(String reason) {
        Objects.requireNonNull(reason, "Fault reason cannot be null");

        if (this.status == DeviceStatus.FAULT) {
            return;
        }

        DeviceStatus previousStatus = this.status;
        this.status = DeviceStatus.FAULT;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new DeviceFaultEvent(
            this.id,
            this.tenantId,
            reason,
            previousStatus,
            this.updatedAt
        ));
    }

    /**
     * 进入维护模式
     */
    public void enterMaintenance() {
        if (this.status == DeviceStatus.MAINTENANCE) {
            return;
        }

        DeviceStatus previousStatus = this.status;
        this.status = DeviceStatus.MAINTENANCE;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new DeviceEnteredMaintenanceEvent(
            this.id,
            this.tenantId,
            previousStatus,
            this.updatedAt
        ));
    }

    /**
     * 退出维护模式
     */
    public void exitMaintenance() {
        if (this.status != DeviceStatus.MAINTENANCE) {
            throw new IllegalStateException("Device is not in maintenance mode");
        }

        this.status = DeviceStatus.ONLINE;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new DeviceExitedMaintenanceEvent(
            this.id,
            this.tenantId,
            this.updatedAt
        ));
    }

    /**
     * 绑定空间
     *
     * 业务规则：
     * - 只有激活后的设备可以绑定空间
     * - 已绑定空间的设备需要先解绑
     */
    public void bindSpace(SpaceId spaceId) {
        Objects.requireNonNull(spaceId, "SpaceId cannot be null");

        if (this.status == DeviceStatus.INACTIVE) {
            throw new IllegalStateException("Cannot bind space to inactive device");
        }

        if (this.spaceId != null && this.spaceId.equals(spaceId)) {
            return; // 幂等性
        }

        SpaceId previousSpaceId = this.spaceId;
        this.spaceId = spaceId;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new DeviceBoundToSpaceEvent(
            this.id,
            this.tenantId,
            previousSpaceId,
            spaceId,
            this.updatedAt
        ));
    }

    /**
     * 解绑空间
     */
    public void unbindSpace() {
        if (this.spaceId == null) {
            return;
        }

        SpaceId previousSpaceId = this.spaceId;
        this.spaceId = null;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new DeviceUnboundFromSpaceEvent(
            this.id,
            this.tenantId,
            previousSpaceId,
            this.updatedAt
        ));
    }

    /**
     * 更新设备状态
     */
    public void updateState(DeviceState newState) {
        Objects.requireNonNull(newState, "DeviceState cannot be null");

        DeviceState previousState = this.state;
        this.state = newState;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new DeviceStateChangedEvent(
            this.id,
            this.tenantId,
            previousState,
            newState,
            this.updatedAt
        ));
    }

    /**
     * 添加遥测数据
     *
     * 说明：写侧仅保留最新快照，历史数据进入读侧时序库
     */
    public void addTelemetry(DeviceTelemetry telemetry) {
        Objects.requireNonNull(telemetry, "Telemetry cannot be null");

        DeviceTelemetry previousTelemetry = this.latestTelemetry;
        this.latestTelemetry = telemetry;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new DeviceTelemetryReportedEvent(
            this.id,
            this.tenantId,
            telemetry.getPropertyIdentifier(),
            telemetry.getValue(),
            telemetry.getReportedAt(),
            telemetry
        ));
    }

    /**
     * 添加设备事件
     */
    public void addEvent(DeviceEvent event) {
        Objects.requireNonNull(event, "DeviceEvent cannot be null");

        DeviceEvent previousEvent = this.latestEvent;
        this.latestEvent = event;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new DeviceEventReportedEvent(
            this.id,
            this.tenantId,
            event.getEventIdentifier(),
            event.getParams(),
            event.getOccurredAt(),
            event
        ));
    }

    /**
     * 发送设备命令
     *
     * @param command 命令对象
     * @return 命令ID
     */
    public CommandId sendCommand(DeviceCommand command) {
        Objects.requireNonNull(command, "Command cannot be null");

        if (this.status != DeviceStatus.ONLINE) {
            throw new IllegalStateException("Device is not online, cannot send command");
        }

        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new DeviceCommandSentEvent(
            this.id,
            this.tenantId,
            command.getId(),
            command.getServiceIdentifier(),
            command.getInputParams(),
            this.updatedAt
        ));

        return command.getId();
    }

    /**
     * 处理命令响应
     */
    public void handleCommandResponse(CommandId commandId,
                                    CommandStatus status,
                                    java.util.Map<String, Object> outputParams) {
        Objects.requireNonNull(commandId, "CommandId cannot be null");
        Objects.requireNonNull(status, "CommandStatus cannot be null");

        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new DeviceCommandRespondedEvent(
            this.id,
            this.tenantId,
            commandId,
            status,
            outputParams,
            this.updatedAt
        ));
    }

    /**
     * OTA升级
     *
     * @param firmwareVersion 固件版本
     */
    public void otaUpgrade(FirmwareVersion firmwareVersion) {
        Objects.requireNonNull(firmwareVersion, "FirmwareVersion cannot be null");

        if (this.status != DeviceStatus.ONLINE) {
            throw new IllegalStateException("Device must be online for OTA upgrade");
        }

        FirmwareVersion previousVersion = this.model.getFirmwareVersion();
        this.model = this.model.withFirmwareVersion(firmwareVersion);
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new DeviceOtaUpgradeStartedEvent(
            this.id,
            this.tenantId,
            previousVersion,
            firmwareVersion,
            this.updatedAt
        ));
    }

    /**
     * OTA升级完成
     */
    public void otaUpgradeCompleted(FirmwareVersion firmwareVersion) {
        Objects.requireNonNull(firmwareVersion, "FirmwareVersion cannot be null");

        registerDomainEvent(new DeviceOtaUpgradeCompletedEvent(
            this.id,
            this.tenantId,
            firmwareVersion,
            LocalDateTime.now()
        ));
    }

    /**
     * OTA升级失败
     */
    public void otaUpgradeFailed(FirmwareVersion firmwareVersion, String reason) {
        Objects.requireNonNull(firmwareVersion, "FirmwareVersion cannot be null");
        Objects.requireNonNull(reason, "Reason cannot be null");

        registerDomainEvent(new DeviceOtaUpgradeFailedEvent(
            this.id,
            this.tenantId,
            firmwareVersion,
            reason,
            LocalDateTime.now()
        ));
    }

    // ============== 领域事件管理 ==============

    /**
     * 注册领域事件
     */
    protected void registerDomainEvent(DomainEvent event) {
        this.domainEvents.add(Objects.requireNonNull(event));
    }

    /**
     * 获取待发布的领域事件
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 清空已发布的领域事件
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    // ============== 查询方法 ==============

    /**
     * 判断是否在线
     */
    public boolean isOnline() {
        return this.status == DeviceStatus.ONLINE;
    }

    /**
     * 判断是否激活
     */
    public boolean isActivated() {
        return this.status != DeviceStatus.INACTIVE;
    }

    /**
     * 判断是否故障
     */
    public boolean isFault() {
        return this.status == DeviceStatus.FAULT;
    }

    /**
     * 判断是否维护中
     */
    public boolean isMaintenance() {
        return this.status == DeviceStatus.MAINTENANCE;
    }

    /**
     * 判断是否已绑定空间
     */
    public boolean isSpaceBound() {
        return this.spaceId != null;
    }

    /**
     * 判断是否可接收命令
     */
    public boolean canAcceptCommand() {
        return this.status == DeviceStatus.ONLINE;
    }
}
```

### 4.2 值对象示例

```java
package com.huaquantomt.device.domain.model.valueobject;

import lombok.Value;

import java.util.regex.Pattern;

/**
 * 设备序列号 - 值对象
 *
 * 业务规则：
 * - 长度：12-20个字符
 * - 格式：大写字母和数字
 * - 去除空格后转大写
 */
@Value
public class DeviceSn {
    private static final Pattern SN_PATTERN = Pattern.compile("^[A-Z0-9]{12,20}$");

    String value;

    private DeviceSn(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("DeviceSn cannot be empty");
        }

        String normalized = value.trim().toUpperCase();

        if (!SN_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                "Invalid DeviceSn format: must be 12-20 uppercase alphanumeric characters"
            );
        }

        this.value = normalized;
    }

    public static DeviceSn of(String value) {
        return new DeviceSn(value);
    }
}
```

```java
package com.huaquantomt.device.domain.model.valueobject;

/**
 * 设备类型 - 枚举值对象
 */
public enum DeviceType {
    // 水务
    WATER_METER("水表"),
    ELECTRIC_METER("电表"),
    GAS_METER("气表"),

    // 传感器
    SMOKE_DETECTOR("烟雾传感器"),
    TEMPERATURE_SENSOR("温度传感器"),
    HUMIDITY_SENSOR("湿度传感器"),
    LIGHT_SENSOR("光照传感器"),
    DOOR_CONTACT("门磁传感器"),

    // 环保
    TRASH_FULL_DETECTOR("垃圾满溢传感器"),

    // 控制
    SOLENOID_VALVE("电磁阀"),
    DOOR_LOCK("门锁"),
    PARKING_LOCK("地锁"),
    AIR_CONDITIONER("空调"),
    LIGHT("灯光"),

    // 畜牧
    ANIMAL_TRACKER("动物定位器"),
    RUMEN_CAPSULE("瘤胃胶囊"),

    // 其他
    GATEWAY("网关"),
    GEOMAGNETIC_DETECTOR("地磁探测器");

    private final String description;

    DeviceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMeter() {
        return this == WATER_METER || this == ELECTRIC_METER || this == GAS_METER;
    }

    public boolean isSensor() {
        return this.name().endsWith("SENSOR") || this == SMOKE_DETECTOR
            || this == TRASH_FULL_DETECTOR || this == DOOR_CONTACT
            || this == GEOMAGNETIC_DETECTOR;
    }

    public boolean isController() {
        return this == SOLENOID_VALVE || this == DOOR_LOCK
            || this == PARKING_LOCK || this == AIR_CONDITIONER
            || this == LIGHT;
    }
}
```

```java
package com.huaquantomt.device.domain.model.valueobject;

import lombok.Value;

import java.util.UUID;

/**
 * 设备ID - 值对象
 */
@Value
public class DeviceId {
    String value;

    private DeviceId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("DeviceId cannot be empty");
        }
        this.value = value;
    }

    public static DeviceId of(String value) {
        return new DeviceId(value);
    }

    public static DeviceId generate() {
        return new DeviceId(UUID.randomUUID().toString());
    }
}
```

### 4.3 领域事件示例

```java
package com.huaquantomt.device.domain.model.domainevent;

import com.huaquantomt.device.domain.model.valueobject.*;
import com.huaquantomt.shared.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 设备激活领域事件
 */
@Getter
public class DeviceActivatedEvent implements DomainEvent {

    private final String eventId = UUID.randomUUID().toString();
    private final LocalDateTime occurredAt;
    private final DeviceId deviceId;
    private final TenantId tenantId;
    private final DeviceStatus previousStatus;

    public DeviceActivatedEvent(DeviceId deviceId,
                               TenantId tenantId,
                               DeviceStatus previousStatus,
                               LocalDateTime occurredAt) {
        this.deviceId = Objects.requireNonNull(deviceId, "DeviceId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.previousStatus = Objects.requireNonNull(previousStatus, "PreviousStatus cannot be null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "OccurredAt cannot be null");
    }

    @Override
    public String getAggregateId() {
        return deviceId.getValue();
    }

    @Override
    public String getAggregateType() {
        return "Device";
    }

    @Override
    public String getTenantId() {
        return tenantId.getValue();
    }

    @Override
    public String getEventType() {
        return "DeviceActivated";
    }
}
```

### 4.4 仓储接口示例

```java
package com.huaquantomt.device.domain.repository;

import com.huaquantomt.device.domain.model.aggregate.Device;
import com.huaquantomt.device.domain.model.valueobject.*;
import com.huaquantomt.shared.domain.repository.BaseRepository;
import com.huaquantomt.shared.domain.repository.PageRequest;
import com.huaquantomt.shared.domain.repository.PageResult;

import java.util.List;
import java.util.Optional;

/**
 * 设备仓储接口
 */
public interface DeviceRepository extends BaseRepository<Device, DeviceId> {

    /**
     * 根据序列号查找设备
     */
    Optional<Device> findBySn(DeviceSn sn);

    /**
     * 根据租户查询设备列表
     */
    List<Device> findByTenant(TenantId tenantId);

    /**
     * 根据租户分页查询设备列表
     */
    PageResult<Device> findByTenant(TenantId tenantId, PageRequest pageRequest);

    /**
     * 根据空间查询设备列表
     */
    List<Device> findBySpace(SpaceId spaceId);

    /**
     * 根据空间分页查询设备列表
     */
    PageResult<Device> findBySpace(SpaceId spaceId, PageRequest pageRequest);

    /**
     * 根据设备类型查询
     */
    List<Device> findByType(DeviceType type);

    /**
     * 根据设备类型分页查询
     */
    PageResult<Device> findByType(DeviceType type, PageRequest pageRequest);

    /**
     * 根据状态查询
     */
    List<Device> findByStatus(DeviceStatus status);

    /**
     * 根据状态分页查询
     */
    PageResult<Device> findByStatus(DeviceStatus status, PageRequest pageRequest);

    /**
     * 根据租户和状态查询
     */
    List<Device> findByTenantAndStatus(TenantId tenantId, DeviceStatus status);

    /**
     * 根据租户和状态分页查询
     */
    PageResult<Device> findByTenantAndStatus(TenantId tenantId,
                                            DeviceStatus status,
                                            PageRequest pageRequest);

    /**
     * 查询租户下在线设备
     */
    List<Device> findOnlineDevicesByTenant(TenantId tenantId);

    /**
     * 查询租户下离线设备
     */
    List<Device> findOfflineDevicesByTenant(TenantId tenantId);

    /**
     * 统计租户下设备数量
     */
    long countByTenant(TenantId tenantId);

    /**
     * 统计租户下指定状态的设备数量
     */
    long countByTenantAndStatus(TenantId tenantId, DeviceStatus status);

    /**
     * 统计租户下指定类型的设备数量
     */
    long countByTenantAndType(TenantId tenantId, DeviceType type);

    /**
     * 检查序列号是否存在
     */
    boolean existsBySn(DeviceSn sn);

    /**
     * 检查租户下设备名称是否存在
     */
    boolean existsByTenantAndName(TenantId tenantId, String name);
}
```

---

## 5. 最佳实践

### 5.1 聚合设计原则

| 原则 | 说明 | 示例 |
|------|------|------|
| **小聚合** | 聚合尽量小，避免性能问题 | Device聚合只包含必要关联 |
| **不变性保护** | 通过构造函数和业务方法保护不变量 | 构造函数验证必需字段 |
| **最终一致性** | 跨聚合通过事件实现最终一致性 | Device与Space通过事件同步 |
| **ID引用** | 跨聚合只引用ID，不引用对象 | Device包含SpaceId而非Space |
| **乐观锁** | 使用版本号实现并发控制 | Device.version字段 |

### 5.2 值对象设计原则

| 原则 | 说明 | 示例 |
|------|------|------|
| **不可变性** | 使用@Value或final字段 | @Value class DeviceSn |
| **自验证** | 构造函数验证业务规则 | DeviceSn验证格式 |
| **替换而非修改** | 创建新实例代替修改 | withXxx()方法 |
| **equals/hashCode** | 基于值比较 | Lombok自动生成 |

### 5.3 领域事件设计原则

| 原则 | 说明 | 示例 |
|------|------|------|
| **过去时命名** | 使用过去分词表示已完成 | DeviceActivatedEvent |
| **包含完整信息** | 事件自包含，无需查询 | 包含previousStatus |
| **幂等性** | 设计幂等键防止重复处理 | idempotencyKey |
| **时间戳** | 记录事件发生时间 | occurredAt |

### 5.4 仓储设计原则

| 原则 | 说明 | 示例 |
|------|------|------|
| **接口隔离** | 仓储接口在domain层 | DeviceRepository在domain |
| **实现分离** | 实现在infrastructure层 | DeviceRepositoryImpl在infrastructure |
| **查询分离** | 读操作使用独立仓储 | DeviceTelemetryReadRepository |
| **分页抽象** | 使用领域分页对象 | PageRequest而非Spring Pageable |

### 5.5 应用服务设计原则

| 原则 | 说明 | 示例 |
|------|------|------|
| **事务边界** | 应用服务是事务边界 | @Transactional在应用服务 |
| **用例编排** | 编排业务流程，不包含逻辑 | 协调多个领域服务 |
| **DTO转换** | 负责DTO与领域对象转换 | DeviceAssembler |
| **异常处理** | 捕获领域异常转换为HTTP状态 | DeviceNotFoundException→404 |

---

## 6. 反模式识别

### 6.1 聚合根反模式

| 反模式 | 问题 | 正确做法 |
|--------|------|----------|
| **贫血模型** | 只有getter/setter，无业务方法 | 业务逻辑放在聚合根内 |
| **大聚合** | 聚合包含过多实体和集合 | 拆分为多个聚合 |
| **跨聚合查询** | 聚合根内直接查询其他聚合 | 使用ID引用，通过领域服务协调 |
| **直接暴露集合** | public List<Entity> getEntities() | 返回不可变视图或防御性拷贝 |

### 6.2 值对象反模式

| 反模式 | 问题 | 正确做法 |
|--------|------|----------|
| **可变值对象** | 使用setter修改值 | 使用@Value确保不可变 |
| **贫血值对象** | 只包装原始类型无验证 | 构造函数验证业务规则 |
| **使用原始类型** | 直接使用String、Long | 创建专门的值对象 |

### 6.3 领域事件反模式

| 反模式 | 问题 | 正确做法 |
|--------|------|----------|
| **命令与事件混淆** | 命名使用祈使句 | 事件使用过去分词 |
| **事件过大** | 事件包含冗余信息 | 只包含必要的业务信息 |
| **同步处理** | 事件发布后同步等待处理 | 异步发布，最终一致性 |
| **缺少幂等键** | 重复处理导致数据错误 | 设计idempotencyKey |

### 6.4 仓储反模式

| 反模式 | 问题 | 正确做法 |
|--------|------|----------|
| **领域层依赖框架** | 仓储接口使用框架类型 | 使用领域类型PageRequest |
| **仓储包含业务逻辑** | 仓储方法包含复杂业务 | 业务逻辑移到领域服务 |
| **N+1查询** | 循环中查询数据库 | 使用批量查询或JOIN |

### 6.5 应用服务反模式

| 反模式 | 问题 | 正确做法 |
|--------|------|----------|
| **业务逻辑泄露** | 应用服务包含业务规则 | 移到领域服务或聚合根 |
| **长事务** | 单个事务处理多个用例 | 拆分为多个小事务 |
| **直接暴露领域对象** | 返回聚合根给接口层 | 转换为DTO |

---

## 附录：检查清单

### 代码审查检查清单

#### 聚合根审查
- [ ] 聚合根是否使用业务命名（无技术后缀）
- [ ] 是否包含版本号字段用于乐观锁
- [ ] 是否有领域事件集合和管理方法
- [ ] 业务方法是否封装了不变量保护
- [ ] 是否正确实现了幂等性
- [ ] 跨聚合引用是否只使用ID

#### 值对象审查
- [ ] 是否使用@Value或final实现不可变
- [ ] 构造函数是否验证业务规则
- [ ] 是否正确实现了equals和hashCode
- [ ] 是否实现了工厂方法（of方法）

#### 领域事件审查
- [ ] 命名是否使用过去分词+Event后缀
- [ ] 是否包含必需字段（eventId, occurredAt等）
- [ ] 是否设计了幂等键
- [ ] 事件是否在正确的时机发布

#### 仓储审查
- [ ] 接口是否在domain层
- [ ] 实现是否在infrastructure层
- [ ] 是否使用领域类型而非框架类型
- [ ] 是否分离了读写仓储

---

**文档结束**
