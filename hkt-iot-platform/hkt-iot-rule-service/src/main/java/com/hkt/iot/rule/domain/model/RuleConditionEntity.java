package com.hkt.iot.rule.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 规则条件实体 (JPA)
 * 基于DDL: rule_condition表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "rule_condition")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RuleConditionEntity extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "condition_code", nullable = false, length = 100)
    private String conditionCode;

    @Column(name = "condition_name", length = 200)
    private String conditionName;

    @Column(name = "condition_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ConditionType conditionType;

    @Column(name = "condition_order", nullable = false)
    private Integer conditionOrder;

    @Column(name = "logic_operator", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private LogicOperator logicOperator;

    @Column(name = "condition_config", columnDefinition = "JSON", nullable = false)
    @Transient
    private Map<String, Object> conditionConfig;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "property_identifier", length = 100)
    private String propertyIdentifier;

    @Column(name = "compare_operator", length = 20)
    @Enumerated(EnumType.STRING)
    private CompareOperator compareOperator;

    @Column(name = "threshold_value", length = 200)
    private String thresholdValue;

    @Column(name = "time_range_config", columnDefinition = "JSON")
    @Transient
    private Map<String, Object> timeRangeConfig;

    @Column(name = "duration_threshold")
    private Integer durationThreshold;

    @Column(name = "duration_required")
    private Boolean durationRequired;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 条件类型
     */
    public enum ConditionType {
        DEVICE_PROPERTY, TIME_RANGE, EXPRESSION, COMPOSITE
    }

    /**
     * 逻辑运算符
     */
    public enum LogicOperator {
        AND, OR, NOT
    }

    /**
     * 比较运算符
     */
    public enum CompareOperator {
        GT, LT, EQ, GTE, LTE, NE, BETWEEN, IN
    }

    /**
     * 工厂方法：创建规则条件
     */
    public static RuleConditionEntity create(
            Long tenantId,
            Long ruleId,
            String ruleCode,
            String conditionCode,
            ConditionType conditionType,
            Integer conditionOrder,
            LogicOperator logicOperator,
            Map<String, Object> conditionConfig) {
        RuleConditionEntity condition = new RuleConditionEntity();
        condition.tenantId = tenantId;
        condition.ruleId = ruleId;
        condition.ruleCode = ruleCode;
        condition.conditionCode = conditionCode;
        condition.conditionType = conditionType;
        condition.conditionOrder = conditionOrder;
        condition.logicOperator = logicOperator;
        condition.conditionConfig = conditionConfig;
        condition.durationRequired = false;
        condition.createdAt = LocalDateTime.now();
        condition.updatedAt = LocalDateTime.now();
        return condition;
    }

    /**
     * 创建设备属性条件
     */
    public static RuleConditionEntity createDevicePropertyCondition(
            Long tenantId,
            Long ruleId,
            String ruleCode,
            String conditionCode,
            Integer conditionOrder,
            LogicOperator logicOperator,
            Long deviceId,
            String propertyIdentifier,
            CompareOperator compareOperator,
            String thresholdValue,
            Integer durationThreshold) {
        RuleConditionEntity condition = new RuleConditionEntity();
        condition.tenantId = tenantId;
        condition.ruleId = ruleId;
        condition.ruleCode = ruleCode;
        condition.conditionCode = conditionCode;
        condition.conditionType = ConditionType.DEVICE_PROPERTY;
        condition.conditionOrder = conditionOrder;
        condition.logicOperator = logicOperator;
        condition.deviceId = deviceId;
        condition.propertyIdentifier = propertyIdentifier;
        condition.compareOperator = compareOperator;
        condition.thresholdValue = thresholdValue;
        condition.durationThreshold = durationThreshold;
        condition.durationRequired = durationThreshold != null && durationThreshold > 0;
        condition.createdAt = LocalDateTime.now();
        condition.updatedAt = LocalDateTime.now();
        return condition;
    }

    /**
     * 更新条件配置
     */
    public void updateConditionConfig(Map<String, Object> conditionConfig) {
        this.conditionConfig = conditionConfig;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新条件顺序
     */
    public void updateConditionOrder(Integer conditionOrder) {
        this.conditionOrder = conditionOrder;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新逻辑运算符
     */
    public void updateLogicOperator(LogicOperator logicOperator) {
        this.logicOperator = logicOperator;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查是否需要持续时间
     */
    public boolean isDurationRequired() {
        return this.durationRequired != null && this.durationRequired;
    }
}
