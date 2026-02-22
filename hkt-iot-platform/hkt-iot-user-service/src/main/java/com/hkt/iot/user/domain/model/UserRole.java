package com.hkt.iot.user.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 * 基于DDL: user_role表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "user_role", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_role", columnNames = {"user_id", "role_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRole extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "granted_by")
    private Long grantedBy;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 工厂方法：创建用户角色关联
     */
    public static UserRole create(
            Long tenantId,
            Long userId,
            Long roleId,
            Long grantedBy,
            LocalDateTime expireAt) {
        UserRole userRole = new UserRole();
        userRole.tenantId = tenantId;
        userRole.userId = userId;
        userRole.roleId = roleId;
        userRole.grantedBy = grantedBy;
        userRole.grantedAt = LocalDateTime.now();
        userRole.expireAt = expireAt;
        userRole.createdAt = LocalDateTime.now();
        return userRole;
    }

    /**
     * 检查是否有效
     */
    public boolean isValid() {
        if (this.expireAt == null) {
            return true;
        }
        return LocalDateTime.now().isBefore(this.expireAt);
    }

    /**
     * 更新到期时间
     */
    public void updateExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }
}
