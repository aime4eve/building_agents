package com.hkt.iot.user.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色权限关联实体
 * 基于DDL: role_permission表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "role_permission", uniqueConstraints = {
    @UniqueConstraint(name = "uk_role_permission", columnNames = {"role_id", "permission_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RolePermission extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

    @Column(name = "granted_by")
    private Long grantedBy;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 工厂方法：创建角色权限关联
     */
    public static RolePermission create(
            Long tenantId,
            Long roleId,
            Long permissionId,
            Long grantedBy) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.tenantId = tenantId;
        rolePermission.roleId = roleId;
        rolePermission.permissionId = permissionId;
        rolePermission.grantedBy = grantedBy;
        rolePermission.grantedAt = LocalDateTime.now();
        rolePermission.createdAt = LocalDateTime.now();
        return rolePermission;
    }
}
