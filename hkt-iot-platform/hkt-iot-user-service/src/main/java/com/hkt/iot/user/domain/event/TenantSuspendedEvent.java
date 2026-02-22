package com.hkt.iot.user.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 租户暂停领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class TenantSuspendedEvent extends DomainEvent {

    private final Long tenantId;
    private final String tenantCode;
    private final LocalDateTime occurredAt;

    public TenantSuspendedEvent(
            Long tenantId,
            String tenantCode,
            LocalDateTime occurredAt) {
        this.tenantId = tenantId;
        this.tenantCode = tenantCode;
        this.occurredAt = occurredAt;
    }

    @Override
    public String eventType() {
        return "TenantSuspended";
    }
}
