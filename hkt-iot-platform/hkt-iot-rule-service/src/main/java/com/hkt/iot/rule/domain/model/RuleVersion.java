package com.hkt.iot.rule.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 规则版本实体
 * 用于管理规则的历史版本
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "rule_version",
    indexes = {
        @Index(name = "idx_rule_id", columnList = "rule_id"),
        @Index(name = "idx_version_number", columnList = "rule_id,version_number")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RuleVersion extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "rule_definition", columnDefinition = "TEXT")
    private String ruleDefinition;

    @Column(name = "rule_config", columnDefinition = "JSON")
    @Transient
    private java.util.Map<String, Object> ruleConfig;

    @Column(name = "trigger_expression", columnDefinition = "TEXT")
    private String triggerExpression;

    @Column(name = "change_description", length = 500)
    private String changeDescription;

    @Column(name = "change_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ChangeType changeType;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 变更类型
     */
    public enum ChangeType {
        CREATE,     // 创建
        UPDATE,     // 更新
        ACTIVATE,   // 激活
        DEACTIVATE, // 停用
        CONFIG      // 配置变更
    }

    /**
     * 工厂方法：创建规则版本
     */
    public static RuleVersion create(
            Long ruleId,
            String ruleCode,
            Integer versionNumber,
            String ruleDefinition,
            java.util.Map<String, Object> ruleConfig,
            String triggerExpression,
            String changeDescription,
            ChangeType changeType,
            Long createdBy) {
        RuleVersion version = new RuleVersion();
        version.ruleId = ruleId;
        version.ruleCode = ruleCode;
        version.versionNumber = versionNumber;
        version.ruleDefinition = ruleDefinition;
        version.ruleConfig = ruleConfig;
        version.triggerExpression = triggerExpression;
        version.changeDescription = changeDescription;
        version.changeType = changeType;
        version.isCurrent = true;
        version.createdBy = createdBy;
        version.createdAt = LocalDateTime.now();
        return version;
    }

    /**
     * 标记为非当前版本
     */
    public void markAsNotCurrent() {
        this.isCurrent = false;
    }

    /**
     * 标记为当前版本
     */
    public void markAsCurrent() {
        this.isCurrent = true;
    }

    /**
     * 检查是否是当前版本
     */
    public boolean isCurrentVersion() {
        return Boolean.TRUE.equals(this.isCurrent);
    }
}
