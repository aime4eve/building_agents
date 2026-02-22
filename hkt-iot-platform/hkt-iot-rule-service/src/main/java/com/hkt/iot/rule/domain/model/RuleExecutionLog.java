package com.hkt.iot.rule.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 规则执行日志实体
 * 基于DDL: rule_execution_log表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "rule_execution_log",
    uniqueConstraints = {@UniqueConstraint(name = "uk_execution_id", columnNames = {"execution_id"})},
    indexes = {
        @Index(name = "idx_rule_id", columnList = "rule_id"),
        @Index(name = "idx_execution_status", columnList = "execution_status"),
        @Index(name = "idx_tenant_status", columnList = "tenant_id,execution_status")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RuleExecutionLog extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "rule_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Rule.RuleType ruleType;

    @Column(name = "execution_id", nullable = false, length = 100)
    private String executionId;

    @Column(name = "trigger_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Rule.TriggerType triggerType;

    @Column(name = "trigger_source", length = 100)
    private String triggerSource;

    @Column(name = "trigger_data", columnDefinition = "JSON")
    @Transient
    private Map<String, Object> triggerData;

    @Column(name = "matched_conditions", columnDefinition = "JSON")
    @Transient
    private List<String> matchedConditions;

    @Column(name = "execution_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ExecutionStatus executionStatus;

    @Column(name = "execution_result", columnDefinition = "JSON")
    @Transient
    private Map<String, Object> executionResult;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "execution_duration")
    private Long executionDuration;

    @Column(name = "total_actions")
    private Integer totalActions;

    @Column(name = "success_actions")
    private Integer successActions;

    @Column(name = "failed_actions")
    private Integer failedActions;

    @Column(name = "space_id")
    private Long spaceId;

    @Column(name = "device_ids", columnDefinition = "JSON")
    @Transient
    private List<Long> deviceIds;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 执行状态
     */
    public enum ExecutionStatus {
        SUCCESS, FAILED, PARTIAL, TIMEOUT
    }

    /**
     * 工厂方法：创建执行日志
     */
    public static RuleExecutionLog create(
            Long tenantId,
            Long ruleId,
            String ruleCode,
            Rule.RuleType ruleType,
            String executionId,
            Rule.TriggerType triggerType,
            String triggerSource,
            Long userId) {
        RuleExecutionLog log = new RuleExecutionLog();
        log.tenantId = tenantId;
        log.ruleId = ruleId;
        log.ruleCode = ruleCode;
        log.ruleType = ruleType;
        log.executionId = executionId;
        log.triggerType = triggerType;
        log.triggerSource = triggerSource;
        log.userId = userId;
        log.triggeredAt = LocalDateTime.now();
        log.executionStatus = ExecutionStatus.SUCCESS;
        log.createdAt = LocalDateTime.now();
        return log;
    }

    /**
     * 开始执行
     */
    public void start() {
        this.startedAt = LocalDateTime.now();
        this.executionStatus = ExecutionStatus.SUCCESS;
    }

    /**
     * 完成执行
     */
    public void complete(ExecutionStatus status, Integer totalActions, Integer successActions, Integer failedActions) {
        this.completedAt = LocalDateTime.now();
        this.executionStatus = status;
        this.totalActions = totalActions;
        this.successActions = successActions;
        this.failedActions = failedActions;
        if (this.startedAt != null) {
            this.executionDuration = java.time.Duration.between(this.startedAt, this.completedAt).toMillis();
        }
    }

    /**
     * 记录失败
     */
    public void fail(String errorCode, String errorMessage) {
        this.executionStatus = ExecutionStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.executionDuration = java.time.Duration.between(this.startedAt, this.completedAt).toMillis();
        }
    }

    /**
     * 记录超时
     */
    public void timeout() {
        this.executionStatus = ExecutionStatus.TIMEOUT;
        this.errorCode = "TIMEOUT";
        this.errorMessage = "规则执行超时";
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.executionDuration = java.time.Duration.between(this.startedAt, this.completedAt).toMillis();
        }
    }

    /**
     * 设置触发数据
     */
    public void setTriggerData(Map<String, Object> triggerData) {
        this.triggerData = triggerData;
    }

    /**
     * 设置匹配的条件
     */
    public void setMatchedConditions(List<String> matchedConditions) {
        this.matchedConditions = matchedConditions;
    }

    /**
     * 设置执行结果
     */
    public void setExecutionResult(Map<String, Object> executionResult) {
        this.executionResult = executionResult;
    }

    /**
     * 检查是否成功
     */
    public boolean isSuccess() {
        return ExecutionStatus.SUCCESS.equals(this.executionStatus);
    }

    /**
     * 检查是否部分成功
     */
    public boolean isPartialSuccess() {
        return ExecutionStatus.PARTIAL.equals(this.executionStatus);
    }

    /**
     * 检查是否失败
     */
    public boolean isFailed() {
        return ExecutionStatus.FAILED.equals(this.executionStatus) ||
               ExecutionStatus.TIMEOUT.equals(this.executionStatus);
    }
}
