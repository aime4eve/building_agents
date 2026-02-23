package com.hkt.iot.workflow.domain.model.entity;

import com.hkt.iot.domain.model.Entity;
import com.hkt.iot.workflow.domain.model.valueobject.TenantId;
import com.hkt.iot.workflow.domain.model.valueobject.WorkOrderType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 自动派单规则实体
 *
 * @author HKT IoT Team
 */
@Getter
@NoArgsConstructor
public class AutoAssignRule extends Entity<String> {

    private String id;
    private String name;
    private WorkOrderType workOrderType;
    private RuleType ruleType;
    private String ruleConfig;
    private Integer priority;
    private Boolean enabled;
    private TenantId tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AutoAssignRule create(
            String name,
            WorkOrderType workOrderType,
            RuleType ruleType,
            String ruleConfig,
            Integer priority,
            TenantId tenantId) {
        AutoAssignRule rule = new AutoAssignRule();
        rule.id = java.util.UUID.randomUUID().toString().replace("-", "");
        rule.name = Objects.requireNonNull(name, "name cannot be null");
        rule.workOrderType = Objects.requireNonNull(workOrderType, "workOrderType cannot be null");
        rule.ruleType = Objects.requireNonNull(ruleType, "ruleType cannot be null");
        rule.ruleConfig = Objects.requireNonNull(ruleConfig, "ruleConfig cannot be null");
        rule.priority = priority != null ? priority : 0;
        rule.enabled = true;
        rule.tenantId = Objects.requireNonNull(tenantId, "tenantId cannot be null");
        rule.createdAt = LocalDateTime.now();
        rule.updatedAt = LocalDateTime.now();
        return rule;
    }

    public void enable() {
        this.enabled = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateConfig(String ruleConfig) {
        this.ruleConfig = Objects.requireNonNull(ruleConfig, "ruleConfig cannot be null");
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePriority(Integer priority) {
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * 规则类型枚举
     */
    public enum RuleType {
        SKILL,
        AREA,
        WORKLOAD
    }
}
