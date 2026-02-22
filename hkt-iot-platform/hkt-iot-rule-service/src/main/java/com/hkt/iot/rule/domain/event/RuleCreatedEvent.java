package com.hkt.iot.rule.domain.event;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 规则已创建事件
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class RuleCreatedEvent {
    private final Long ruleId;
    private final String ruleCode;
    private final String ruleName;
    private final Long tenantId;
    private final LocalDateTime occurredAt;

    public RuleCreatedEvent(Long ruleId, String ruleCode, String ruleName,
                           Long tenantId, LocalDateTime occurredAt) {
        this.ruleId = ruleId;
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.tenantId = tenantId;
        this.occurredAt = occurredAt;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleCreatedEvent that = (RuleCreatedEvent) o;
        return Objects.equals(ruleId, that.ruleId) &&
                Objects.equals(occurredAt, that.occurredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId, occurredAt);
    }

    @Override
    public String toString() {
        return "RuleCreatedEvent{" +
                "ruleId=" + ruleId +
                ", ruleCode='" + ruleCode + '\'' +
                ", tenantId=" + tenantId +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
