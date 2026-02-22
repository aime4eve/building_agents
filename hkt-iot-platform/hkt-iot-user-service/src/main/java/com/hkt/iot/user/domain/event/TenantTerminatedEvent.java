package com.hkt.iot.user.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 租户终止领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class TenantTerminatedEvent extends DomainEvent {

    private final Long tenantId;
    private final String tenantCode;
    private final LocalDateTime occurredAt;

    public TenantTerminatedEvent(
            Long tenantId,
            String tenantCode,
            LocalDateTime occurredAt) {
        this.tenantId = tenantId;
        this.tenantCode = tenantCode;
        this.occurredAt = occurredAt;
    }

    @Override
    public String eventType() {
        return "TenantTerminated";
    }
}
