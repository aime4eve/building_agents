package com.hkt.iot.rule.domain.event;

import com.hkt.iot.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * 规则被触发事件
 * 当规则条件匹配成功时发布
 *
 * @author HKT IoT Team
 */
@Getter
public class RuleTriggeredEvent extends AbstractDomainEvent {

    private final Long ruleId;
    private final String ruleCode;
    private final String ruleName;
    private final Long tenantId;
    private final String ruleType;
    private final Map<String, Object> triggerContext;
    private final Instant triggeredAt;

    public RuleTriggeredEvent(
            Long ruleId,
            String ruleCode,
            String ruleName,
            Long tenantId,
            String ruleType,
            Map<String, Object> triggerContext,
            Instant triggeredAt) {
        this.ruleId = ruleId;
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.tenantId = tenantId;
        this.ruleType = ruleType;
        this.triggerContext = triggerContext;
        this.triggeredAt = triggeredAt != null ? triggeredAt : Instant.now();
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
        return "RuleTriggered";
    }
}
