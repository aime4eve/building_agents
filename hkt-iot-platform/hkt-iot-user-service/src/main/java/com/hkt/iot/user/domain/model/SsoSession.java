package com.hkt.iot.user.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SSO会话实体
 * 基于DDL: sso_session表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "sso_session", uniqueConstraints = {
    @UniqueConstraint(name = "uk_session_id", columnNames = {"session_id"}),
    @UniqueConstraint(name = "uk_session_token", columnNames = {"session_token"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SsoSession extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "session_token", nullable = false, length = 255)
    private String sessionToken;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_active_at", nullable = false)
    private LocalDateTime lastActiveAt;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    /**
     * 会话状态
     */
    public enum SessionStatus {
        ACTIVE, EXPIRED, LOGOUT
    }

    /**
     * 工厂方法：创建会话
     */
    public static SsoSession create(
            String sessionId,
            String sessionToken,
            Long userId,
            Long tenantId,
            String clientId,
            String deviceType,
            String deviceId,
            String ipAddress,
            String userAgent,
            LocalDateTime expiresAt) {
        SsoSession session = new SsoSession();
        session.sessionId = sessionId;
        session.sessionToken = sessionToken;
        session.userId = userId;
        session.tenantId = tenantId;
        session.clientId = clientId;
        session.deviceType = deviceType;
        session.deviceId = deviceId;
        session.ipAddress = ipAddress;
        session.userAgent = userAgent;
        session.createdAt = LocalDateTime.now();
        session.expiresAt = expiresAt;
        session.lastActiveAt = LocalDateTime.now();
        session.status = SessionStatus.ACTIVE;
        return session;
    }

    /**
     * 更新最后活跃时间
     */
    public void updateLastActiveTime() {
        this.lastActiveAt = LocalDateTime.now();
    }

    /**
     * 登出
     */
    public void logout() {
        this.status = SessionStatus.LOGOUT;
        this.lastActiveAt = LocalDateTime.now();
    }

    /**
     * 过期
     */
    public void expire() {
        this.status = SessionStatus.EXPIRED;
        this.lastActiveAt = LocalDateTime.now();
    }

    /**
     * 检查是否有效
     */
    public boolean isValid() {
        if (this.status != SessionStatus.ACTIVE) {
            return false;
        }
        return LocalDateTime.now().isBefore(this.expiresAt);
    }

    /**
     * 检查是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 延长有效期
     */
    public void extend(LocalDateTime newExpiresAt) {
        this.expiresAt = newExpiresAt;
        this.lastActiveAt = LocalDateTime.now();
    }
}
