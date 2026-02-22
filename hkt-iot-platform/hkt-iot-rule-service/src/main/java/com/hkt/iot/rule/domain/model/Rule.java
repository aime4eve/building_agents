package com.hkt.iot.rule.domain.model;

import com.hkt.iot.domain.model.AggregateRoot;
import com.hkt.iot.rule.domain.event.RuleCreatedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 规则聚合根
 * 基于DDL: rule表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "rule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rule extends AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 200)
    private String ruleName;

    @Column(name = "rule_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RuleType ruleType;

    @Column(name = "rule_category", length = 50)
    private String ruleCategory;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "rule_priority")
    private Integer rulePriority;

    @Column(name = "trigger_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TriggerType triggerType;

    @Column(name = "trigger_expression", columnDefinition = "TEXT")
    private String triggerExpression;

    @Column(name = "rule_config", nullable = false, columnDefinition = "JSON")
    @Transient
    private Map<String, Object> ruleConfig;

    @Column(name = "rule_definition", columnDefinition = "JSON")
    @Transient
    private String ruleDefinition;

    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "device_ids", columnDefinition = "JSON")
    @Transient
    private List<Long> deviceIds;

    @Column(name = "effective_time")
    private LocalDateTime effectiveTime;

    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "schedule_config", columnDefinition = "JSON")
    @Transient
    private Map<String, Object> scheduleConfig;

    @Column(name = "rule_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RuleStatus ruleStatus;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Column(name = "total_executions")
    private Long totalExecutions;

    @Column(name = "success_executions")
    private Long successExecutions;

    @Column(name = "failed_executions")
    private Long failedExecutions;

    @Column(name = "last_execution_time")
    private LocalDateTime lastExecutionTime;

    @Column(name = "last_execution_status", length = 20)
    private String lastExecutionStatus;

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
     * 规则类型
     */
    public enum RuleType {
        ALARM, LINKAGE, BILLING, CONTROL
    }

    /**
     * 触发方式
     */
    public enum TriggerType {
        REALTIME, SCHEDULED, MANUAL
    }

    /**
     * 规则状态
     */
    public enum RuleStatus {
        DRAFT, ACTIVE, INACTIVE, ARCHIVED
    }

    /**
     * 工厂方法：创建规则
     */
    public static Rule create(
            Long tenantId,
            String ruleCode,
            String ruleName,
            RuleType ruleType,
            String ruleCategory,
            String description,
            TriggerType triggerType,
            Map<String, Object> ruleConfig,
            List<Long> deviceIds,
            Long createdBy) {
        Rule rule = new Rule();
        rule.tenantId = tenantId;
        rule.ruleCode = ruleCode;
        rule.ruleName = ruleName;
        rule.ruleType = ruleType;
        rule.ruleCategory = ruleCategory;
        rule.description = description;
        rule.triggerType = triggerType;
        rule.ruleConfig = ruleConfig;
        rule.deviceIds = deviceIds;
        rule.ruleStatus = RuleStatus.DRAFT;
        rule.isEnabled = false;
        rule.rulePriority = 5;
        rule.totalExecutions = 0L;
        rule.successExecutions = 0L;
        rule.failedExecutions = 0L;
        rule.deleted = false;
        rule.createdAt = LocalDateTime.now();
        rule.updatedAt = LocalDateTime.now();
        rule.createdBy = createdBy;
        rule.updatedBy = createdBy;
        rule.version = 0L;

        // 发布规则创建事件
        rule.registerDomainEvent(new RuleCreatedEvent(
                rule.id,
                rule.ruleCode,
                rule.ruleName,
                rule.tenantId,
                rule.createdAt
        ));

        return rule;
    }

    /**
     * 启用规则
     */
    public void enable() {
        if (this.ruleStatus != RuleStatus.DRAFT && this.ruleStatus != RuleStatus.INACTIVE) {
            throw new IllegalStateException("只有草稿或非活跃状态的规则才能启用");
        }
        this.isEnabled = true;
        this.ruleStatus = RuleStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 禁用规则
     */
    public void disable() {
        if (this.ruleStatus != RuleStatus.ACTIVE) {
            throw new IllegalStateException("只有活跃状态的规则才能禁用");
        }
        this.isEnabled = false;
        this.ruleStatus = RuleStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新执行统计
     */
    public void updateExecutionStats(boolean success) {
        this.totalExecutions++;
        if (success) {
            this.successExecutions++;
        } else {
            this.failedExecutions++;
        }
        this.lastExecutionTime = LocalDateTime.now();
        this.lastExecutionStatus = success ? "SUCCESS" : "FAILED";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 归档规则
     */
    public void archive() {
        this.ruleStatus = RuleStatus.ARCHIVED;
        this.isEnabled = false;
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

    /**
     * 设置规则名称
     */
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    /**
     * 设置描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 设置触发表达式
     */
    public void setTriggerExpression(String triggerExpression) {
        this.triggerExpression = triggerExpression;
    }

    /**
     * 设置规则配置
     */
    public void setRuleConfig(Map<String, Object> ruleConfig) {
        this.ruleConfig = ruleConfig;
    }

    /**
     * 设置设备ID列表
     */
    public void setDeviceIds(List<Long> deviceIds) {
        this.deviceIds = deviceIds;
    }

    /**
     * 设置生效时间
     */
    public void setEffectiveTime(LocalDateTime effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    /**
     * 设置过期时间
     */
    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    /**
     * 设置Cron表达式
     */
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    /**
     * 设置更新时间
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 设置更新者
     */
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
}
