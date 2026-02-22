package com.hkt.iot.rule.domain.event;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 规则已激活事件
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class RuleActivatedEvent {
    private final Long ruleId;
    private final LocalDateTime occurredAt;

    public RuleActivatedEvent(Long ruleId, LocalDateTime occurredAt) {
        this.ruleId = ruleId;
        this.occurredAt = occurredAt;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleActivatedEvent that = (RuleActivatedEvent) o;
        return Objects.equals(ruleId, that.ruleId) &&
                Objects.equals(occurredAt, that.occurredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId, occurredAt);
    }

    @Override
    public String toString() {
        return "RuleActivatedEvent{" +
                "ruleId=" + ruleId +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
