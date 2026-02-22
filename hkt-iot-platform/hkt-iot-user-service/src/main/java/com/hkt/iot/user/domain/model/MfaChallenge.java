package com.hkt.iot.user.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MFA 挑战实体
 * 基于 DDL: mfa_challenge 表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "mfa_challenge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MfaChallenge extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "challenge_code", nullable = false, length = 100)
    private String challengeCode;

    @Column(name = "mfa_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MfaConfig.MfaType mfaType;

    @Column(name = "code", length = 20)
    private String code;

    @Column(name = "verification_method", length = 50)
    private String verificationMethod;

    @Column(name = "client_id", length = 100)
    private String clientId;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ChallengeStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "attempt_count")
    private Integer attemptCount;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 挑战状态
     */
    public enum ChallengeStatus {
        PENDING, VERIFIED, FAILED, EXPIRED
    }

    /**
     * 工厂方法：创建 MFA 挑战（简化版）
     */
    public static MfaChallenge create(
            Long userId,
            Long tenantId,
            String challengeCode,
            MfaConfig.MfaType mfaType,
            String code,
            String verificationMethod,
            LocalDateTime expiresAt) {
        MfaChallenge challenge = new MfaChallenge();
        challenge.userId = userId;
        challenge.tenantId = tenantId;
        challenge.challengeCode = challengeCode;
        challenge.mfaType = mfaType;
        challenge.code = code;
        challenge.verificationMethod = verificationMethod;
        challenge.status = ChallengeStatus.PENDING;
        challenge.expiresAt = expiresAt;
        challenge.attemptCount = 0;
        challenge.maxAttempts = 3;
        challenge.createdAt = LocalDateTime.now();
        return challenge;
    }

    /**
     * 工厂方法：创建 MFA 挑战（完整参数）
     */
    public static MfaChallenge create(
            Long userId,
            Long tenantId,
            String challengeCode,
            MfaConfig.MfaType mfaType,
            String code,
            String verificationMethod,
            String clientId,
            String userAgent,
            String deviceType,
            String deviceId,
            LocalDateTime expiresAt) {
        MfaChallenge challenge = new MfaChallenge();
        challenge.userId = userId;
        challenge.tenantId = tenantId;
        challenge.challengeCode = challengeCode;
        challenge.mfaType = mfaType;
        challenge.code = code;
        challenge.verificationMethod = verificationMethod;
        challenge.clientId = clientId;
        challenge.userAgent = userAgent;
        challenge.deviceType = deviceType;
        challenge.deviceId = deviceId;
        challenge.status = ChallengeStatus.PENDING;
        challenge.expiresAt = expiresAt;
        challenge.attemptCount = 0;
        challenge.maxAttempts = 3;
        challenge.createdAt = LocalDateTime.now();
        return challenge;
    }

    /**
     * 验证 MFA 码（兼容方法）
     */
    public boolean verifyCode(String inputCode) {
        return verify(inputCode);
    }

    /**
     * 验证
     */
    public boolean verify(String inputCode) {
        if (this.status != ChallengeStatus.PENDING) {
            return false;
        }
        if (this.isExpired()) {
            this.expire();
            return false;
        }
        if (this.attemptCount >= this.maxAttempts) {
            this.fail();
            return false;
        }
        this.attemptCount++;
        if (this.code.equals(inputCode)) {
            this.status = ChallengeStatus.VERIFIED;
            this.verifiedAt = LocalDateTime.now();
            return true;
        }
        if (this.attemptCount >= this.maxAttempts) {
            this.fail();
        }
        return false;
    }

    /**
     * 记录失败尝试
     */
    public void recordFailedAttempt() {
        this.attemptCount = this.attemptCount != null ? this.attemptCount + 1 : 1;
        if (this.attemptCount >= this.maxAttempts) {
            this.fail();
        }
    }

    /**
     * 标记为已验证
     */
    public void markAsVerified() {
        this.status = ChallengeStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    /**
     * 失败
     */
    public void fail() {
        this.status = ChallengeStatus.FAILED;
        this.failedAt = LocalDateTime.now();
    }

    /**
     * 过期
     */
    public void expire() {
        this.status = ChallengeStatus.EXPIRED;
    }

    /**
     * 检查是否过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * 检查是否可以重试
     */
    public boolean canRetry() {
        return this.status == ChallengeStatus.PENDING &&
               this.attemptCount < this.maxAttempts &&
               !this.isExpired();
    }

    /**
     * 取消挑战
     */
    public void cancel() {
        if (this.status == ChallengeStatus.PENDING) {
            this.status = ChallengeStatus.FAILED;
        }
    }
}
