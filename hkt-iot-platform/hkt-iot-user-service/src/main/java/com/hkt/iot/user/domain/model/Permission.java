package com.hkt.iot.user.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 权限实体
 * 基于DDL: permission表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "permission", uniqueConstraints = {
    @UniqueConstraint(name = "uk_permission_code", columnNames = {"permission_code"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission_code", nullable = false, length = 100)
    private String permissionCode;

    @Column(name = "permission_name", nullable = false, length = 200)
    private String permissionName;

    @Column(name = "resource_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Column(name = "resource_path", length = 200)
    private String resourcePath;

    @Column(name = "action", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Action action;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "permission_path", length = 500)
    private String permissionPath;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PermissionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 资源类型
     */
    public enum ResourceType {
        MENU, API, BUTTON, DATA
    }

    /**
     * 操作类型
     */
    public enum Action {
        CREATE, READ, UPDATE, DELETE, EXECUTE
    }

    /**
     * 权限状态
     */
    public enum PermissionStatus {
        ACTIVE, INACTIVE
    }

    /**
     * 工厂方法：创建权限
     */
    public static Permission create(
            String permissionCode,
            String permissionName,
            ResourceType resourceType,
            String resourcePath,
            Action action,
            Long parentId,
            String description) {
        Permission permission = new Permission();
        permission.permissionCode = permissionCode;
        permission.permissionName = permissionName;
        permission.resourceType = resourceType;
        permission.resourcePath = resourcePath;
        permission.action = action;
        permission.parentId = parentId;
        permission.description = description;
        permission.displayOrder = 0;
        permission.status = PermissionStatus.ACTIVE;
        permission.createdAt = LocalDateTime.now();
        permission.updatedAt = LocalDateTime.now();
        permission.updatePermissionPath();
        return permission;
    }

    /**
     * 更新权限路径
     */
    public void updatePermissionPath() {
        if (this.parentId == null) {
            this.permissionPath = "/" + this.permissionCode;
        } else {
            this.permissionPath = this.permissionPath + "/" + this.permissionCode;
        }
    }

    /**
     * 更新权限信息
     */
    public void updatePermissionInfo(String permissionName, String description, String resourcePath) {
        this.permissionName = permissionName;
        this.description = description;
        this.resourcePath = resourcePath;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 激活
     */
    public void activate() {
        this.status = PermissionStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 停用
     */
    public void deactivate() {
        this.status = PermissionStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新显示顺序
     */
    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
        this.updatedAt = LocalDateTime.now();
    }
}
