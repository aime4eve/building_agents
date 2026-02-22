package com.hkt.iot.rule.domain.event;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 规则已暂停事件
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class RuleSuspendedEvent {
    private final Long ruleId;
    private final String reason;
    private final LocalDateTime occurredAt;

    public RuleSuspendedEvent(Long ruleId, String reason, LocalDateTime occurredAt) {
        this.ruleId = ruleId;
        this.reason = reason;
        this.occurredAt = occurredAt;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleSuspendedEvent that = (RuleSuspendedEvent) o;
        return Objects.equals(ruleId, that.ruleId) &&
                Objects.equals(occurredAt, that.occurredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId, occurredAt);
    }

    @Override
    public String toString() {
        return "RuleSuspendedEvent{" +
                "ruleId=" + ruleId +
                ", reason='" + reason + '\'' +
                ", occurredAt=" + occurredAt +
                '}';
    }
}
