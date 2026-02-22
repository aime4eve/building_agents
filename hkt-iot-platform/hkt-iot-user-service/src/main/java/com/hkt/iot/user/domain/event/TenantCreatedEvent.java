package com.hkt.iot.user.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 租户创建领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class TenantCreatedEvent extends DomainEvent {

    private final Long tenantId;
    private final String tenantCode;
    private final String tenantName;
    private final Tenant.TenantType tenantType;
    private final LocalDateTime createdAt;

    /**
     * 租户类型枚举
     */
    public enum TenantType {
        OPERATOR, GROUP, SUBSIDIARY, ENTERPRISE
    }

    public TenantCreatedEvent(
            Long tenantId,
            String tenantCode,
            String tenantName,
            TenantType tenantType,
            LocalDateTime createdAt) {
        this.tenantId = tenantId;
        this.tenantCode = tenantCode;
        this.tenantName = tenantName;
        this.tenantType = tenantType;
        this.createdAt = createdAt;
    }

    @Override
    public String eventType() {
        return "TenantCreated";
    }
}
