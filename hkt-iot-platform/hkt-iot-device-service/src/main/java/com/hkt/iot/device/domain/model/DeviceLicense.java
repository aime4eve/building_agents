package com.hkt.iot.device.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 设备License实体
 * 基于DDL: device_license表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "device_license")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "device_sn", nullable = false, length = 100)
    private String deviceSn;

    @Column(name = "license_key", nullable = false, length = 200)
    private String licenseKey;

    @Column(name = "license_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private LicenseType licenseType;

    @Column(name = "license_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private LicenseStatus licenseStatus;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "authorized_features", columnDefinition = "JSON")
    @Transient
    private List<String> authorizedFeatures;

    @Column(name = "max_devices")
    private Integer maxDevices;

    @Column(name = "billing_cycle", length = 20)
    private String billingCycle;

    @Column(name = "fee_amount", precision = 10, scale = 2)
    private BigDecimal feeAmount;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew;

    @Column(name = "last_renew_date")
    private LocalDate lastRenewDate;

    @Column(name = "next_billing_date")
    private LocalDate nextBillingDate;

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
     * License类型
     */
    public enum LicenseType {
        STANDARD, PREMIUM, ENTERPRISE
    }

    /**
     * License状态
     */
    public enum LicenseStatus {
        ACTIVE, SUSPENDED, EXPIRED, REVOKED
    }

    /**
     * 工厂方法：创建License
     */
    public static DeviceLicense create(
            Long tenantId,
            Long deviceId,
            String deviceSn,
            String licenseKey,
            LicenseType licenseType,
            LocalDate startDate,
            LocalDate endDate,
            List<String> authorizedFeatures,
            Integer maxDevices,
            Long createdBy) {
        DeviceLicense license = new DeviceLicense();
        license.tenantId = tenantId;
        license.deviceId = deviceId;
        license.deviceSn = deviceSn;
        license.licenseKey = licenseKey;
        license.licenseType = licenseType;
        license.licenseStatus = LicenseStatus.ACTIVE;
        license.startDate = startDate;
        license.endDate = endDate;
        license.authorizedFeatures = authorizedFeatures;
        license.maxDevices = maxDevices;
        license.currency = "CNY";
        license.autoRenew = false;
        license.deleted = false;
        license.createdAt = LocalDateTime.now();
        license.updatedAt = LocalDateTime.now();
        license.createdBy = createdBy;
        license.updatedBy = createdBy;
        license.version = 0L;
        return license;
    }

    /**
     * 检查是否过期
     */
    public boolean isExpired() {
        if (endDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * 暂停License
     */
    public void suspend() {
        this.licenseStatus = LicenseStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 激活License
     */
    public void activate() {
        this.licenseStatus = LicenseStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 续费
     */
    public void renew(LocalDate newEndDate, BigDecimal feeAmount) {
        this.endDate = newEndDate;
        this.feeAmount = feeAmount;
        this.lastRenewDate = LocalDate.now();
        this.nextBillingDate = newEndDate;
        this.licenseStatus = LicenseStatus.ACTIVE;
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
