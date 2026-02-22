package com.hkt.iot.user.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MFA配置实体
 * 基于DDL: mfa_config表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "mfa_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MfaConfig extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "mfa_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MfaType mfaType;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;

    @Column(name = "secret_key", length = 255)
    private String secretKey;

    @Column(name = "backup_codes", columnDefinition = "JSON")
    @Transient
    private List<String> backupCodes;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MfaStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * MFA类型
     */
    public enum MfaType {
        TOTP, SMS, EMAIL, HARDWARE_TOKEN
    }

    /**
     * MFA状态
     */
    public enum MfaStatus {
        ACTIVE, INACTIVE
    }

    /**
     * 工厂方法：创建MFA配置
     */
    public static MfaConfig create(
            Long userId,
            Long tenantId,
            MfaType mfaType,
            String secretKey) {
        MfaConfig config = new MfaConfig();
        config.userId = userId;
        config.tenantId = tenantId;
        config.mfaType = mfaType;
        config.secretKey = secretKey;
        config.isEnabled = false;
        config.isPrimary = false;
        config.status = MfaStatus.INACTIVE;
        config.createdAt = LocalDateTime.now();
        config.updatedAt = LocalDateTime.now();
        return config;
    }

    /**
     * 启用
     */
    public void enable() {
        this.isEnabled = true;
        this.status = MfaStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 禁用
     */
    public void disable() {
        this.isEnabled = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设为主要方式
     */
    public void setAsPrimary() {
        this.isPrimary = true;
        this.isEnabled = true;
        this.status = MfaStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新密钥
     */
    public void updateSecretKey(String secretKey) {
        this.secretKey = secretKey;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 生成备用恢复码
     */
    public void generateBackupCodes(List<String> backupCodes) {
        this.backupCodes = backupCodes;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查是否已启用
     */
    public boolean isConfigured() {
        return this.isEnabled && MfaStatus.ACTIVE.equals(this.status);
    }
}
