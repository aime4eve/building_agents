package com.hkt.iot.rule.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 规则启用领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class RuleEnabledEvent extends DomainEvent {

    private final Long ruleId;
    private final String ruleCode;
    private final Long tenantId;
    private final LocalDateTime enabledAt;

    public RuleEnabledEvent(
            Long ruleId,
            String ruleCode,
            Long tenantId,
            LocalDateTime enabledAt) {
        this.ruleId = ruleId;
        this.ruleCode = ruleCode;
        this.tenantId = tenantId;
        this.enabledAt = enabledAt;
    }

    @Override
    public String eventType() {
        return "RuleEnabled";
    }
}
