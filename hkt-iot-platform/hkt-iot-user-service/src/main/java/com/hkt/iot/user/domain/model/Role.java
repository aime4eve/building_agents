package com.hkt.iot.user.domain.model;

import com.hkt.iot.domain.model.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 角色聚合根
 * 基于DDL: role表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "role", uniqueConstraints = {
    @UniqueConstraint(name = "uk_tenant_role_code", columnNames = {"tenant_id", "role_code", "deleted"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "role_code", nullable = false, length = 100)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(name = "role_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RoleStatus status;

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
     * 角色类型
     */
    public enum RoleType {
        SYSTEM, CUSTOM, BUSINESS
    }

    /**
     * 角色状态
     */
    public enum RoleStatus {
        ACTIVE, INACTIVE
    }

    /**
     * 工厂方法：创建角色
     */
    public static Role create(
            Long tenantId,
            String roleCode,
            String roleName,
            RoleType roleType,
            String description,
            Long createdBy) {
        Role role = new Role();
        role.tenantId = tenantId;
        role.roleCode = roleCode;
        role.roleName = roleName;
        role.roleType = roleType;
        role.description = description;
        role.isDefault = false;
        role.displayOrder = 0;
        role.status = RoleStatus.ACTIVE;
        role.deleted = false;
        role.createdAt = LocalDateTime.now();
        role.updatedAt = LocalDateTime.now();
        role.createdBy = createdBy;
        role.updatedBy = createdBy;
        role.version = 0L;
        return role;
    }

    /**
     * 更新角色信息
     */
    public void updateRoleInfo(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 激活
     */
    public void activate() {
        this.status = RoleStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 停用
     */
    public void deactivate() {
        if (this.isDefault) {
            throw new IllegalStateException("默认角色不能停用");
        }
        this.status = RoleStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置为默认角色
     */
    public void setAsDefault() {
        this.isDefault = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新显示顺序
     */
    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 软删除
     */
    public void softDelete(Long deletedBy) {
        if (this.isDefault) {
            throw new IllegalStateException("默认角色不能删除");
        }
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.updatedAt = LocalDateTime.now();
    }
}
