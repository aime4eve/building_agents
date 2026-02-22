package com.hkt.iot.rule.domain.model;

import com.hkt.iot.domain.model.AggregateRoot;
import com.hkt.iot.rule.domain.event.RuleSetCreatedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 规则集聚合根
 * 管理一组相关的规则，支持批量操作和统一管理
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "rule_set",
    indexes = {
        @Index(name = "idx_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_space_id", columnList = "space_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RuleSet extends AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "set_code", nullable = false, length = 100)
    private String setCode;

    @Column(name = "set_name", nullable = false, length = 200)
    private String setName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "set_category", length = 50)
    private String setCategory;

    @Column(name = "space_id")
    private Long spaceId;

    /**
     * 规则ID列表（JSON存储）
     */
    @Column(name = "rule_ids", columnDefinition = "JSON")
    @Transient
    private Set<Long> ruleIds;

    /**
     * 规则集优先级
     */
    @Column(name = "priority")
    private Integer priority;

    /**
     * 规则集状态
     */
    @Column(name = "set_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RuleSetStatus setStatus;

    /**
     * 规则执行策略
     */
    @Column(name = "execution_strategy", length = 50)
    @Enumerated(EnumType.STRING)
    private ExecutionStrategy executionStrategy;

    /**
     * 是否启用并行执行
     */
    @Column(name = "parallel_enabled")
    private Boolean parallelEnabled;

    /**
     * 最大并行数
     */
    @Column(name = "max_parallel")
    private Integer maxParallel;

    /**
     * 超时时间（秒）
     */
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

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

    /**
     * 规则集状态
     */
    public enum RuleSetStatus {
        DRAFT, ACTIVE, INACTIVE, ARCHIVED
    }

    /**
     * 执行策略
     */
    public enum ExecutionStrategy {
        ALL,        // 执行所有规则
        ANY,        // 任一规则匹配即停止
        FIRST,      // 第一个规则匹配后停止
        SEQUENTIAL  // 按顺序执行，失败则停止
    }

    /**
     * 工厂方法：创建规则集
     */
    public static RuleSet create(
            Long tenantId,
            String setCode,
            String setName,
            String description,
            String setCategory,
            Long spaceId,
            ExecutionStrategy executionStrategy,
            Long createdBy) {
        RuleSet ruleSet = new RuleSet();
        ruleSet.tenantId = tenantId;
        ruleSet.setCode = setCode;
        ruleSet.setName = setName;
        ruleSet.description = description;
        ruleSet.setCategory = setCategory;
        ruleSet.spaceId = spaceId;
        ruleSet.executionStrategy = executionStrategy;
        ruleSet.ruleIds = new HashSet<>();
        ruleSet.setStatus = RuleSetStatus.DRAFT;
        ruleSet.priority = 5;
        ruleSet.parallelEnabled = false;
        ruleSet.maxParallel = 5;
        ruleSet.timeoutSeconds = 30;
        ruleSet.version = 0L;
        ruleSet.createdAt = LocalDateTime.now();
        ruleSet.updatedAt = LocalDateTime.now();
        ruleSet.createdBy = createdBy;
        ruleSet.updatedBy = createdBy;

        // 发布规则集创建事件
        ruleSet.registerDomainEvent(new RuleSetCreatedEvent(
                ruleSet.id,
                ruleSet.setCode,
                ruleSet.setName,
                ruleSet.tenantId,
                ruleSet.createdAt
        ));

        return ruleSet;
    }

    /**
     * 添加规则到规则集
     */
    public void addRule(Long ruleId) {
        if (this.ruleIds == null) {
            this.ruleIds = new HashSet<>();
        }
        this.ruleIds.add(ruleId);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 批量添加规则
     */
    public void addRules(Set<Long> ruleIds) {
        if (this.ruleIds == null) {
            this.ruleIds = new HashSet<>();
        }
        this.ruleIds.addAll(ruleIds);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 从规则集移除规则
     */
    public void removeRule(Long ruleId) {
        if (this.ruleIds != null) {
            this.ruleIds.remove(ruleId);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 清空规则集
     */
    public void clearRules() {
        if (this.ruleIds != null) {
            this.ruleIds.clear();
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 激活规则集
     */
    public void activate() {
        if (this.setStatus != RuleSetStatus.DRAFT && this.setStatus != RuleSetStatus.INACTIVE) {
            throw new IllegalStateException("只有草稿或非活跃状态的规则集才能激活");
        }
        this.setStatus = RuleSetStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 停用规则集
     */
    public void deactivate() {
        if (this.setStatus != RuleSetStatus.ACTIVE) {
            throw new IllegalStateException("只有活跃状态的规则集才能停用");
        }
        this.setStatus = RuleSetStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 归档规则集
     */
    public void archive() {
        this.setStatus = RuleSetStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新基本信息
     */
    public void updateInfo(String setName, String description, Integer priority) {
        if (setName != null) {
            this.setName = setName;
        }
        if (description != null) {
            this.description = description;
        }
        if (priority != null) {
            this.priority = priority;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置执行策略
     */
    public void setExecutionStrategy(ExecutionStrategy strategy, Boolean parallel, Integer maxParallel) {
        this.executionStrategy = strategy;
        if (parallel != null) {
            this.parallelEnabled = parallel;
        }
        if (maxParallel != null) {
            this.maxParallel = maxParallel;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 获取规则数量
     */
    public int getRuleCount() {
        return ruleIds != null ? ruleIds.size() : 0;
    }

    /**
     * 检查是否包含指定规则
     */
    public boolean containsRule(Long ruleId) {
        return ruleIds != null && ruleIds.contains(ruleId);
    }

    /**
     * 获取所有规则ID
     */
    public Set<Long> getRuleIds() {
        return ruleIds != null ? new HashSet<>(ruleIds) : Set.of();
    }
}
