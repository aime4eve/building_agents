package com.hkt.iot.user.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MFA挑战实体
 * 基于DDL: mfa_challenge表
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
     * 工厂方法：创建MFA挑战
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
