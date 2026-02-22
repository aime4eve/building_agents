package com.hkt.iot.rule.domain.event;

import com.hkt.iot.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.time.Instant;

/**
 * 规则集创建事件
 *
 * @author HKT IoT Team
 */
@Getter
public class RuleSetCreatedEvent extends AbstractDomainEvent {

    private final Long ruleSetId;
    private final String setCode;
    private final String setName;
    private final Long tenantId;
    private final Instant createdAt;

    public RuleSetCreatedEvent(
            Long ruleSetId,
            String setCode,
            String setName,
            Long tenantId,
            Instant createdAt) {
        this.ruleSetId = ruleSetId;
        this.setCode = setCode;
        this.setName = setName;
        this.tenantId = tenantId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    @Override
    public String getAggregateId() {
        return ruleSetId != null ? ruleSetId.toString() : null;
    }

    @Override
    public String getAggregateType() {
        return "RuleSet";
    }

    @Override
    public String eventType() {
        return "RuleSetCreated";
    }
}
