package com.hkt.iot.device.domain.model;

import com.hkt.iot.domain.model.AggregateRoot;
import com.hkt.iot.device.domain.event.DeviceRegisteredEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 设备聚合根
 * 基于DDL: device表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "device")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Device extends AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "device_sn", nullable = false, length = 100)
    private String deviceSn;

    @Column(name = "device_name", nullable = false, length = 200)
    private String deviceName;

    @Column(name = "device_code", length = 100)
    private String deviceCode;

    @Column(name = "device_type", nullable = false, length = 50)
    private String deviceType;

    @Column(name = "device_model", nullable = false, length = 100)
    private String deviceModel;

    @Column(name = "device_category", nullable = false, length = 50)
    private String deviceCategory;

    @Column(name = "thing_model_id")
    private Long thingModelId;

    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "parent_device_id")
    private Long parentDeviceId;

    @Column(name = "device_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DeviceStatus deviceStatus;

    @Column(name = "online_status", nullable = false)
    private Boolean onlineStatus;

    @Column(name = "activation_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ActivationStatus activationStatus;

    @Column(name = "location_desc", length = 500)
    private String locationDesc;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "altitude", precision = 8, scale = 2)
    private BigDecimal altitude;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "mac_address", length = 50)
    private String macAddress;

    @Column(name = "gateway_id")
    private Long gatewayId;

    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;

    @Column(name = "hardware_version", length = 50)
    private String hardwareVersion;

    @Column(name = "software_version", length = 50)
    private String softwareVersion;

    @Column(name = "latest_properties", columnDefinition = "JSON")
    @Transient
    private Map<String, Object> latestProperties;

    @Column(name = "latest_event", columnDefinition = "JSON")
    @Transient
    private Map<String, Object> latestEvent;

    @Column(name = "last_online_time")
    private LocalDateTime lastOnlineTime;

    @Column(name = "last_offline_time")
    private LocalDateTime lastOfflineTime;

    @Column(name = "last_data_time")
    private LocalDateTime lastDataTime;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    @Column(name = "locked_by")
    private Long lockedBy;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "lock_reason", length = 500)
    private String lockReason;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    /**
     * 设备状态枚举
     */
    public enum DeviceStatus {
        ONLINE, OFFLINE, FAULT, MAINTENANCE, INACTIVE
    }

    /**
     * 激活状态枚举
     */
    public enum ActivationStatus {
        ACTIVE, INACTIVE
    }

    /**
     * 工厂方法：创建设备
     */
    public static Device create(
            Long tenantId,
            String deviceSn,
            String deviceName,
            String deviceType,
            String deviceModel,
            String deviceCategory,
            Long createdBy) {
        Device device = new Device();
        device.tenantId = tenantId;
        device.deviceSn = deviceSn;
        device.deviceName = deviceName;
        device.deviceType = deviceType;
        device.deviceModel = deviceModel;
        device.deviceCategory = deviceCategory;
        device.deviceStatus = DeviceStatus.INACTIVE;
        device.onlineStatus = false;
        device.activationStatus = ActivationStatus.INACTIVE;
        device.isLocked = false;
        device.deleted = false;
        device.createdAt = LocalDateTime.now();
        device.updatedAt = LocalDateTime.now();
        device.createdBy = createdBy;
        device.updatedBy = createdBy;
        device.version = 0L;

        // 发布设备注册事件
        device.registerDomainEvent(new DeviceRegisteredEvent(
                device.id,
                device.deviceSn,
                device.tenantId,
                device.createdAt
        ));

        return device;
    }

    /**
     * 设备上线
     */
    public void goOnline(String ipAddress) {
        this.deviceStatus = DeviceStatus.ONLINE;
        this.onlineStatus = true;
        this.lastOnlineTime = LocalDateTime.now();
        this.ipAddress = ipAddress;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设备离线
     */
    public void goOffline() {
        this.deviceStatus = DeviceStatus.OFFLINE;
        this.onlineStatus = false;
        this.lastOfflineTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新遥测数据快照
     */
    public void updateTelemetrySnapshot(Map<String, Object> properties) {
        this.latestProperties = properties;
        this.lastDataTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新事件快照
     */
    public void updateEventSnapshot(Map<String, Object> event) {
        this.latestEvent = event;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 激活设备
     */
    public void activate() {
        if (this.activationStatus == ActivationStatus.ACTIVE) {
            return;
        }
        this.activationStatus = ActivationStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 停用设备
     */
    public void deactivate() {
        if (this.activationStatus == ActivationStatus.INACTIVE) {
            return;
        }
        this.activationStatus = ActivationStatus.INACTIVE;
        this.deviceStatus = DeviceStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 锁定设备
     */
    public void lock(Long lockedBy, String reason) {
        if (this.isLocked) {
            throw new IllegalStateException("设备已被锁定");
        }
        this.isLocked = true;
        this.lockedBy = lockedBy;
        this.lockReason = reason;
        this.lockedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 解锁设备
     */
    public void unlock() {
        this.isLocked = false;
        this.lockedBy = null;
        this.lockReason = null;
        this.lockedAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 软删除
     */
    public void softDelete(Long deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.updatedAt = LocalDateTime.now();
    }
}
