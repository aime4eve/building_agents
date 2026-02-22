package com.hkt.iot.rule.domain.event;

import com.hkt.iot.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.time.Instant;

/**
 * 规则执行失败事件
 * 当规则执行过程中发生错误时发布
 *
 * @author HKT IoT Team
 */
@Getter
public class RuleExecutionFailedEvent extends AbstractDomainEvent {

    private final Long ruleId;
    private final String ruleCode;
    private final Long tenantId;
    private final String errorMessage;
    private final String errorType;
    private final StackTraceElement[] stackTrace;
    private final Instant failedAt;

    public RuleExecutionFailedEvent(
            Long ruleId,
            String ruleCode,
            Long tenantId,
            String errorMessage,
            String errorType,
            StackTraceElement[] stackTrace,
            Instant failedAt) {
        this.ruleId = ruleId;
        this.ruleCode = ruleCode;
        this.tenantId = tenantId;
        this.errorMessage = errorMessage;
        this.errorType = errorType;
        this.stackTrace = stackTrace;
        this.failedAt = failedAt != null ? failedAt : Instant.now();
    }

    public RuleExecutionFailedEvent(
            Long ruleId,
            String ruleCode,
            Long tenantId,
            Exception exception) {
        this(ruleId, ruleCode, tenantId,
                exception.getMessage(),
                exception.getClass().getSimpleName(),
                exception.getStackTrace(),
                Instant.now());
    }

    @Override
    public String getAggregateId() {
        return ruleId != null ? ruleId.toString() : null;
    }

    @Override
    public String getAggregateType() {
        return "Rule";
    }

    @Override
    public String eventType() {
        return "RuleExecutionFailed";
    }
}
