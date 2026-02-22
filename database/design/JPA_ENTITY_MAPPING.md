# JPA实体类映射指南

## 版本信息
- 版本: V1.0
- 创建日期: 2026-02-20
- 基于DDL版本: V1.1

---

## 目录
1. [映射原则](#映射原则)
2. [基础实体类](#基础实体类)
3. [设备管理模块实体](#设备管理模块实体)
4. [用户与租户模块实体](#用户与租户模块实体)
5. [值对象映射](#值对象映射)
6. [仓储接口定义](#仓储接口定义)

---

## 映射原则

### 1. 实体类命名规范

| DDL表名 | JPA实体类 | 说明 |
|---------|-----------|------|
| device | Device | 设备聚合根 |
| device_thing_model | DeviceThingModel | 物模型实体 |
| device_license | DeviceLicense | License实体 |
| tenant | Tenant | 租户聚合根 |
| user | User | 用户实体 |

### 2. 字段类型映射

| MySQL类型 | Java类型 | JPA注解 |
|-----------|----------|---------|
| BIGINT UNSIGNED | Long | @Id @GeneratedValue |
| VARCHAR(n) | String | @Column(length=n) |
| DATETIME(3) | LocalDateTime | @Column |
| TINYINT | Integer/Boolean | @Column |
| JSON | String | @Column(columnDefinition="JSON") |
| DECIMAL(10,2) | BigDecimal | @Column(precision=10, scale=2) |

### 3. 乐观锁实现

```java
@Version
private Long version;
```

---

## 基础实体类

### 审计基类

```java
package com.huakuantong.iot.platform.shared.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 审计基类
 * 包含创建时间、更新时间、创建人、更新人
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class Auditable {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 软删除基类

```java
package com.huakuantong.iot.platform.shared.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 软删除基类
 * 包含删除标记、删除时间、删除人
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class SoftDeletable extends Auditable {

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    /**
     * 软删除
     */
    public void softDelete(String deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * 恢复
     */
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }
}
```

### 租户基类

```java
package com.huakuantong.iot.platform.shared.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 租户基类
 * 包含租户ID，用于多租户数据隔离
 */
@MappedSuperclass
@Getter
@Setter
public abstract class TenantScoped {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "tenant_code", length = 100)
    private String tenantCode;
}
```

---

## 设备管理模块实体

### Device 设备聚合根

```java
package com.huakuantong.iot.platform.device.domain.aggregate;

import com.huakuantong.iot.platform.device.domain.vo.*;
import com.huakuantong.iot.platform.shared.domain.SoftDeletable;
import com.huakuantong.iot.platform.shared.domain.TenantScoped;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.DomainEvents;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 设备聚合根
 */
@Entity
@Table(name = "device", indexes = {
    @Index(name = "idx_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_device_type", columnList = "device_type"),
    @Index(name = "idx_device_status", columnList = "device_status"),
    @Index(name = "idx_online_status", columnList = "online_status"),
    @Index(name = "idx_tenant_status_online", columnList = "tenant_id, device_status, online_status")
})
@Getter
@Setter
public class Device extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 值对象 - 嵌入ID
    @Embedded
    private DeviceId deviceId;

    // 值对象 - 设备序列号
    @Embedded
    @AttributeOverride(name = "value")
    private DeviceSn deviceSn;

    @Column(name = "device_name", nullable = false, length = 200)
    private String deviceName;

    @Column(name = "device_code", length = 100)
    private String deviceCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 50)
    private DeviceType deviceType;

    @Column(name = "device_model", nullable = false, length = 100)
    private String deviceModel;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_category", nullable = false, length = 50)
    private DeviceCategory deviceCategory;

    // 关联关系
    @Column(name = "thing_model_id")
    private Long thingModelId;

    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "parent_device_id")
    private Long parentDeviceId;

    // 设备状态
    @Enumerated(EnumType.STRING)
    @Column(name = "device_status", nullable = false, length = 20)
    private DeviceStatus deviceStatus = DeviceStatus.INACTIVE;

    @Column(name = "online_status", nullable = false)
    private Integer onlineStatus = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "activation_status", nullable = false, length = 20)
    private ActivationStatus activationStatus = ActivationStatus.INACTIVE;

    // 位置信息
    @Column(name = "location_desc", length = 500)
    private String locationDesc;

    @Column(name = "longitude", precision = 10, scale = 7)
    private java.math.BigDecimal longitude;

    @Column(name = "latitude", precision = 10, scale = 7)
    private java.math.BigDecimal latitude;

    // 最新状态快照（JSON格式）
    @Column(name = "latest_properties", columnDefinition = "JSON")
    private String latestProperties;

    @Column(name = "last_online_time")
    private LocalDateTime lastOnlineTime;

    @Column(name = "last_offline_time")
    private LocalDateTime lastOfflineTime;

    @Column(name = "last_data_time")
    private LocalDateTime lastDataTime;

    // 版本控制（乐观锁）
    @Version
    @Column(name = "version")
    private Long version = 0L;

    // 领域事件
    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    // 业务方法
    public void activate() {
        if (this.deviceStatus == DeviceStatus.INACTIVE) {
            this.deviceStatus = DeviceStatus.ONLINE;
            this.activationStatus = ActivationStatus.ACTIVE;
            this.lastOnlineTime = LocalDateTime.now();
            // 发布事件
            domainEvents.add(new DeviceActivatedEvent(this.deviceId));
        }
    }

    public void deactivate() {
        if (this.deviceStatus != DeviceStatus.INACTIVE) {
            this.deviceStatus = DeviceStatus.INACTIVE;
            this.lastOfflineTime = LocalDateTime.now();
            domainEvents.add(new DeviceDeactivatedEvent(this.deviceId));
        }
    }

    public void enterMaintenance() {
        this.deviceStatus = DeviceStatus.MAINTENANCE;
        domainEvents.add(new DeviceEnteredMaintenanceEvent(this.deviceId));
    }

    public void updateProperties(Map<String, Object> properties) {
        this.latestProperties = JsonUtil.toJson(properties);
        this.lastDataTime = LocalDateTime.now();
    }
}
```

### DeviceSn 值对象

```java
package com.huakuantong.iot.platform.device.domain.vo;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * 设备序列号值对象
 */
@Embeddable
@EqualsAndHashCode
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceSn {

    @Column(name = "device_sn", nullable = false, length = 100)
    private String value;

    public DeviceSn(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Device SN cannot be empty");
        }
        this.value = value;
    }

    public static DeviceSn of(String value) {
        return new DeviceSn(value);
    }
}
```

---

## 用户与租户模块实体

### Tenant 租户聚合根

```java
package com.huakuantong.iot.platform.user.domain.aggregate;

import com.huakuantong.iot.platform.shared.domain.SoftDeletable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.DomainEvents;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 租户聚合根
 */
@Entity
@Table(name = "tenant", indexes = {
    @Index(name = "idx_tenant_type", columnList = "tenant_type"),
    @Index(name = "idx_tenant_status", columnList = "tenant_status"),
    @Index(name = "idx_parent_tenant_id", columnList = "parent_tenant_id")
})
@Getter
@Setter
public class Tenant extends SoftDeletable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tenant_code", nullable = false, length = 100, unique = true)
    private String tenantCode;

    @Column(name = "tenant_name", nullable = false, length = 200)
    private String tenantName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_type", nullable = false, length = 20)
    private TenantType tenantType;

    @Column(name = "parent_tenant_id")
    private Long parentTenantId;

    @Column(name = "tenant_path", length = 500)
    private String tenantPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_status", nullable = false, length = 20)
    private TenantStatus tenantStatus = TenantStatus.ACTIVE;

    @Column(name = "max_users")
    private Integer maxUsers = 100;

    @Column(name = "max_devices")
    private Integer maxDevices = 1000;

    @Version
    @Column(name = "version")
    private Long version = 0L;

    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    // 业务方法
    public void suspend() {
        if (this.tenantStatus == TenantStatus.ACTIVE) {
            this.tenantStatus = TenantStatus.SUSPENDED;
            domainEvents.add(new TenantSuspendedEvent(this.id));
        }
    }

    public void activate() {
        if (this.tenantStatus != TenantStatus.ACTIVE) {
            this.tenantStatus = TenantStatus.ACTIVE;
            domainEvents.add(new TenantActivatedEvent(this.id));
        }
    }
}
```

---

## 仓储接口定义

### 基础仓储接口

```java
package com.huakuantong.iot.platform.shared.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * 基础仓储接口
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * 根据ID查找（忽略软删除）
     */
    Optional<T> findByIdAndDeletedFalse(ID id);

    /**
     * 检查是否存在（忽略软删除）
     */
    boolean existsByIdAndDeletedFalse(ID id);
}
```

### 乐观锁仓储接口

```java
package com.huakuantong.iot.platform.shared.domain;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * 乐观锁仓储接口
 */
@NoRepositoryBean
public interface OptimisticLockRepository<T, ID> extends BaseRepository<T, ID> {

    /**
     * 使用乐观锁更新实体
     * @return 更新成功返回true，版本冲突返回false
     */
    @Modifying
    @Query("UPDATE #{#entity} SET #{#fields} WHERE id = :id AND version = :version")
    int updateWithVersion(T entity, Long version);
}
```

### Device 仓储接口

```java
package com.huakuantong.iot.platform.device.domain.repository;

import com.huakuantong.iot.platform.device.domain.aggregate.Device;
import com.huakuantong.iot.platform.device.domain.vo.DeviceId;
import com.huakuantong.iot.platform.device.domain.vo.DeviceSn;
import com.huakuantong.iot.platform.device.domain.vo.DeviceStatus;
import com.huakuantong.iot.platform.shared.domain.OptimisticLockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 设备仓储接口
 */
public interface DeviceRepository extends OptimisticLockRepository<Device, Long> {

    /**
     * 根据设备序列号查找
     */
    Optional<Device> findByDeviceSn(DeviceSn deviceSn);

    /**
     * 根据设备ID查找
     */
    Optional<Device> findByDeviceId(DeviceId deviceId);

    /**
     * 检查设备序列号是否存在
     */
    boolean existsByDeviceSn(DeviceSn deviceSn);

    /**
     * 根据租户和状态查找设备
     */
    List<Device> findByTenantIdAndDeviceStatus(Long tenantId, DeviceStatus status);

    /**
     * 根据租户和状态分页查找设备
     */
    Page<Device> findByTenantIdAndDeviceStatus(Long tenantId, DeviceStatus status, Pageable pageable);

    /**
     * 查询租户下在线设备
     */
    List<Device> findByTenantIdAndOnlineStatus(Long tenantId, Integer onlineStatus);

    /**
     * 统计租户下的设备数量
     */
    long countByTenantId(Long tenantId);

    /**
     * 统计租户下指定状态的设备数量
     */
    long countByTenantIdAndDeviceStatus(Long tenantId, DeviceStatus status);
}
```

---

## 应用配置

### application.yml 配置示例

```yaml
spring:
  datasource:
    # MySQL主数据源配置
    mysql:
      url: jdbc:mysql://localhost:3306/huakuantong_agent?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
      username: huakuantong
      password: hkt123456
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        minimum-idle: 5
        maximum-pool-size: 20
        idle-timeout: 600000
        connection-timeout: 30000
        max-lifetime: 1800000

    # TDengine数据源配置（可选）
    tdengine:
      url: jdbc:TAOS://localhost:6030/telemetry
      username: root
      password: taosdata
      driver-class-name: com.taosdata.jdbc.TSDBDriver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        use_sql_comments: true

# 时序数据库配置
timeseries:
  influx:
    enabled: true
    url: http://localhost:8086
    token: my-super-secret-auth-token
    org: huakuantong
    bucket: huakuantong_telemetry

  tdengine:
    enabled: false
    url: jdbc:TAOS://localhost:6030/telemetry
    username: root
    password: taosdata
    database: telemetry

# JPA审计配置
spring:
  data:
    jpa:
      repositories:
        enabled: true
  jpa:
    auditing:
      enable: true
```

---

## 使用示例

### 创建设备实体

```java
@Service
@RequiredArgsConstructor
public class DeviceApplicationService {

    private final DeviceRepository deviceRepository;

    @Transactional
    public DeviceId createDevice(CreateDeviceCommand command) {
        // 创建值对象
        DeviceSn deviceSn = DeviceSn.of(command.getDeviceSn());
        DeviceId deviceId = DeviceId.generate();

        // 创建聚合根
        Device device = new Device();
        device.setDeviceId(deviceId);
        device.setDeviceSn(deviceSn);
        device.setDeviceName(command.getDeviceName());
        device.setDeviceType(DeviceType.SENSOR);
        device.setTenantId(command.getTenantId());

        // 保存
        return deviceRepository.save(device).getDeviceId();
    }
}
```

### 乐观锁更新

```java
@Service
@RequiredArgsConstructor
public class DeviceControlService {

    private final DeviceRepository deviceRepository;

    @Transactional
    public void controlDevice(ControlDeviceCommand command) {
        // 获取设备
        Device device = deviceRepository.findByDeviceId(command.getDeviceId())
            .orElseThrow(() -> new DeviceNotFoundException(command.getDeviceId()));

        // 记录原始版本号
        Long originalVersion = device.getVersion();

        // 执行控制逻辑
        device.control(command.getServiceIdentifier(), command.getParams());

        // 保存（自动检查版本号）
        try {
            deviceRepository.save(device);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConcurrentModificationException("设备已被修改，请重试");
        }
    }
}
```

---

## 枚举定义

### DeviceStatus 枚举

```java
package com.huakuantong.iot.platform.device.domain.vo;

/**
 * 设备状态枚举
 */
public enum DeviceStatus {
    ONLINE,      // 在线
    OFFLINE,     // 离线
    FAULT,       // 故障
    MAINTENANCE, // 维护
    INACTIVE     // 未激活
}
```

### TenantType 枚举

```java
package com.huakuantong.iot.platform.user.domain.vo;

/**
 * 租户类型枚举
 */
public enum TenantType {
    OPERATOR,     // 运营商
    GROUP,        // 集团租户
    SUBSIDIARY,   // 子公司租户
    ENTERPRISE    // 企业租户
}
```

---

## 附录：完整的注解说明

| 注解 | 说明 | 示例 |
|------|------|------|
| @Entity | 标记为JPA实体 | @Entity |
| @Table | 指定表名 | @Table(name = "device") |
| @Id | 主键 | @Id |
| @GeneratedValue | 主键生成策略 | @GeneratedValue(strategy = GenerationType.IDENTITY) |
| @Column | 列映射 | @Column(name = "device_name", length = 200) |
| @Embedded | 嵌入对象 | @Embedded |
| @Enumerated | 枚举映射 | @Enumerated(EnumType.STRING) |
| @Transient | 忽略字段 | @Transient |
| @Version | 乐观锁版本号 | @Version |
| @Index | 索引定义 | @Index(name = "idx_tenant_id", columnList = "tenant_id") |
| @PrePersist | 保存前回调 | @PrePersist |
| @PreUpdate | 更新前回调 | @PreUpdate |
