package com.hkt.iot.user.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户实体
 * 基于DDL: user表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "tenant_code", nullable = false, length = 100)
    private String tenantCode;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "real_name", length = 100)
    private String realName;

    @Column(name = "email", nullable = false, length = 200)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "salt", length = 100)
    private String salt;

    @Column(name = "user_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @Column(name = "account_type", length = 20)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = "is_mfa_enabled", nullable = false)
    private Boolean isMfaEnabled;

    @Column(name = "mfa_secret", length = 100)
    private String mfaSecret;

    @Column(name = "password_updated_at")
    private LocalDateTime passwordUpdatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "failed_login_count")
    private Integer failedLoginCount;

    @Column(name = "avatar", length = 500)
    private String avatar;

    @Column(name = "department", length = 200)
    private String department;

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "employee_id", length = 100)
    private String employeeId;

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
     * 用户状态
     */
    public enum UserStatus {
        ACTIVE, INACTIVE, LOCKED
    }

    /**
     * 账号类型
     */
    public enum AccountType {
        NORMAL, ADMIN, SUPER_ADMIN
    }

    /**
     * 工厂方法：创建用户
     */
    public static User create(
            Long tenantId,
            String tenantCode,
            String username,
            String realName,
            String email,
            String phone,
            String encodedPassword,
            String salt,
            Long createdBy) {
        User user = new User();
        user.tenantId = tenantId;
        user.tenantCode = tenantCode;
        user.username = username;
        user.realName = realName;
        user.email = email;
        user.phone = phone;
        user.password = encodedPassword;
        user.salt = salt;
        user.userStatus = UserStatus.ACTIVE;
        user.accountType = AccountType.NORMAL;
        user.isMfaEnabled = false;
        user.failedLoginCount = 0;
        user.deleted = false;
        user.createdAt = LocalDateTime.now();
        user.updatedAt = LocalDateTime.now();
        user.createdBy = createdBy;
        user.updatedBy = createdBy;
        user.version = 0L;
        return user;
    }

    /**
     * 验证密码
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return this.password.equals(encodedPassword);
    }

    /**
     * 更新密码
     */
    public void updatePassword(String encodedPassword, String salt) {
        this.password = encodedPassword;
        this.salt = salt;
        this.passwordUpdatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 记录登录
     */
    public void recordLogin(String ipAddress) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
        this.failedLoginCount = 0;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 记录登录失败
     */
    public void recordFailedLogin() {
        this.failedLoginCount++;
        this.updatedAt = LocalDateTime.now();

        // 连续失败5次锁定账户30分钟
        if (this.failedLoginCount >= 5) {
            this.lock(LocalDateTime.now().plusMinutes(30));
        }
    }

    /**
     * 锁定账户
     */
    public void lock(LocalDateTime lockedUntil) {
        this.userStatus = UserStatus.LOCKED;
        this.lockedAt = LocalDateTime.now();
        this.lockedUntil = lockedUntil;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 解锁账户
     */
    public void unlock() {
        this.userStatus = UserStatus.ACTIVE;
        this.lockedAt = null;
        this.lockedUntil = null;
        this.failedLoginCount = 0;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 激活MFA
     */
    public void enableMfa(String mfaSecret) {
        this.isMfaEnabled = true;
        this.mfaSecret = mfaSecret;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 禁用MFA
     */
    public void disableMfa() {
        this.isMfaEnabled = false;
        this.mfaSecret = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 停用用户
     */
    public void deactivate() {
        if (this.userStatus == UserStatus.INACTIVE) {
            return;
        }
        this.userStatus = UserStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 激活用户
     */
    public void activate() {
        if (this.userStatus == UserStatus.ACTIVE) {
            return;
        }
        this.userStatus = UserStatus.ACTIVE;
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
