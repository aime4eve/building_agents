package com.hkt.iot.user.domain.model;

import com.hkt.iot.domain.model.AggregateRoot;
import com.hkt.iot.user.domain.event.TenantCreatedEvent;
import com.hkt.iot.user.domain.event.TenantSuspendedEvent;
import com.hkt.iot.user.domain.event.TenantTerminatedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 租户聚合根
 * 基于 DDL: tenant 表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "tenant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tenant extends AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_code", nullable = false, length = 100)
    private String tenantCode;

    @Column(name = "tenant_name", nullable = false, length = 200)
    private String tenantName;

    @Column(name = "tenant_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TenantType tenantType;

    @Column(name = "parent_tenant_id")
    private Long parentTenantId;

    @Column(name = "tenant_path", length = 500)
    private String tenantPath;

    @Column(name = "tenant_level")
    private Integer tenantLevel;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_email", length = 200)
    private String contactEmail;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "company_size", length = 50)
    private String companySize;

    @Column(name = "business_license", length = 100)
    private String businessLicense;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_devices")
    private Integer maxDevices;

    @Column(name = "max_spaces")
    private Integer maxSpaces;

    @Column(name = "storage_quota")
    private Long storageQuota;

    @Column(name = "tenant_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TenantStatus tenantStatus;

    @Column(name = "activate_date")
    private LocalDateTime activateDate;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;

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
     * 租户类型
     */
    public enum TenantType {
        OPERATOR, GROUP, SUBSIDIARY, ENTERPRISE
    }

    /**
     * 租户状态
     */
    public enum TenantStatus {
        ACTIVE, SUSPENDED, TERMINATED
    }

    /**
     * 工厂方法：创建租户
     */
    public static Tenant create(
            String tenantCode,
            String tenantName,
            TenantType tenantType,
            String contactPerson,
            String contactPhone,
            String contactEmail,
            Long createdBy) {
        Tenant tenant = new Tenant();
        tenant.tenantCode = tenantCode;
        tenant.tenantName = tenantName;
        tenant.tenantType = tenantType;
        tenant.contactPerson = contactPerson;
        tenant.contactPhone = contactPhone;
        tenant.contactEmail = contactEmail;
        tenant.tenantStatus = TenantStatus.ACTIVE;
        tenant.maxUsers = 100;
        tenant.maxDevices = 1000;
        tenant.maxSpaces = 100;
        tenant.storageQuota = 1073741824L; // 1GB
        tenant.deleted = false;
        tenant.createdAt = LocalDateTime.now();
        tenant.updatedAt = LocalDateTime.now();
        tenant.createdBy = createdBy;
        tenant.updatedBy = createdBy;
        tenant.version = 0L;

        // 设置租户路径
        if (tenant.tenantType == TenantType.OPERATOR) {
            tenant.tenantPath = "/" + tenant.tenantCode;
            tenant.tenantLevel = 1;
        }

        // 发布租户创建事件
        tenant.registerDomainEvent(new TenantCreatedEvent(
                tenant.id,
                tenant.tenantCode,
                tenant.tenantName,
                tenant.tenantType,
                tenant.createdAt
        ));

        return tenant;
    }

    /**
     * 设置父租户
     */
    public void setParentTenant(Long parentTenantId, String parentPath, Integer parentLevel) {
        this.parentTenantId = parentTenantId;
        this.tenantPath = parentPath + "/" + this.tenantCode;
        this.tenantLevel = parentLevel + 1;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 激活租户
     */
    public void activate() {
        if (this.tenantStatus == TenantStatus.ACTIVE) {
            return;
        }
        this.tenantStatus = TenantStatus.ACTIVE;
        this.activateDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 暂停租户
     */
    public void suspend() {
        if (this.tenantStatus == TenantStatus.SUSPENDED) {
            return;
        }
        this.tenantStatus = TenantStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
        
        // 发布租户暂停事件
        registerDomainEvent(new TenantSuspendedEvent(
                this.id,
                this.tenantCode,
                LocalDateTime.now()
        ));
    }

    /**
     * 终止租户
     */
    public void terminate() {
        if (this.tenantStatus == TenantStatus.TERMINATED) {
            return;
        }
        this.tenantStatus = TenantStatus.TERMINATED;
        this.updatedAt = LocalDateTime.now();
        
        // 发布租户终止事件
        registerDomainEvent(new TenantTerminatedEvent(
                this.id,
                this.tenantCode,
                LocalDateTime.now()
        ));
    }

    /**
     * 更新配额
     */
    public void updateQuota(Integer maxUsers, Integer maxDevices, Integer maxSpaces, Long storageQuota) {
        this.maxUsers = maxUsers;
        this.maxDevices = maxDevices;
        this.maxSpaces = maxSpaces;
        this.storageQuota = storageQuota;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查用户配额
     */
    public boolean checkUserQuota(long currentUserCount) {
        return currentUserCount < this.maxUsers;
    }

    /**
     * 检查设备配额
     */
    public boolean checkDeviceQuota(long currentDeviceCount) {
        return currentDeviceCount < this.maxDevices;
    }

    /**
     * 检查空间配额
     */
    public boolean checkSpaceQuota(long currentSpaceCount) {
        return currentSpaceCount < this.maxSpaces;
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
