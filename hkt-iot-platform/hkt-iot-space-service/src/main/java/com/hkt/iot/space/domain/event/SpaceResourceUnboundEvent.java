package com.hkt.iot.space.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 空间资源解绑领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class SpaceResourceUnboundEvent extends DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final Long spaceId;
    private final String spaceCode;
    private final Long tenantId;
    private final SpaceResource.ResourceType resourceType;
    private final Long resourceId;
    private final String resourceCode;
    private final String unboundReason;
    private final LocalDateTime unboundAt;
    private final Long unboundBy;

    public SpaceResourceUnboundEvent(
            Long spaceId,
            String spaceCode,
            Long tenantId,
            SpaceResource.ResourceType resourceType,
            Long resourceId,
            String resourceCode,
            String unboundReason,
            LocalDateTime unboundAt,
            Long unboundBy) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.spaceId = spaceId;
        this.spaceCode = spaceCode;
        this.tenantId = tenantId;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.resourceCode = resourceCode;
        this.unboundReason = unboundReason;
        this.unboundAt = unboundAt;
        this.unboundBy = unboundBy;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String getAggregateId() {
        return String.valueOf(spaceId);
    }

    @Override
    public String getAggregateType() {
        return "Space";
    }

    @Override
    public String eventType() {
        return "SpaceResourceUnbound";
    }
}
