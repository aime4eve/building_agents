package com.hkt.iot.user.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MFA设备实体
 * 基于DDL: mfa_device表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "mfa_device", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_device", columnNames = {"user_id", "device_identifier"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MfaDevice extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "device_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Column(name = "device_identifier", nullable = false, length = 200)
    private String deviceIdentifier;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DeviceStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 设备类型
     */
    public enum DeviceType {
        HARDWARE_TOKEN, BIOSENSOR
    }

    /**
     * 设备状态
     */
    public enum DeviceStatus {
        ACTIVE, INACTIVE, LOST
    }

    /**
     * 工厂方法：创建MFA设备
     */
    public static MfaDevice create(
            Long userId,
            Long tenantId,
            String deviceName,
            DeviceType deviceType,
            String deviceIdentifier) {
        MfaDevice device = new MfaDevice();
        device.userId = userId;
        device.tenantId = tenantId;
        device.deviceName = deviceName;
        device.deviceType = deviceType;
        device.deviceIdentifier = deviceIdentifier;
        device.isVerified = false;
        device.status = DeviceStatus.INACTIVE;
        device.createdAt = LocalDateTime.now();
        device.updatedAt = LocalDateTime.now();
        return device;
    }

    /**
     * 验证通过
     */
    public void verify() {
        this.isVerified = true;
        this.status = DeviceStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 记录使用
     */
    public void recordUsage() {
        this.lastUsedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 激活
     */
    public void activate() {
        this.status = DeviceStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 停用
     */
    public void deactivate() {
        this.status = DeviceStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 标记丢失
     */
    public void markAsLost() {
        this.status = DeviceStatus.LOST;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查是否可用
     */
    public boolean isAvailable() {
        return this.isVerified && DeviceStatus.ACTIVE.equals(this.status);
    }
}
