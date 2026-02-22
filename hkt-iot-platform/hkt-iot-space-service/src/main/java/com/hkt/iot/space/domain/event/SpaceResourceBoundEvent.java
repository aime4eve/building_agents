package com.hkt.iot.space.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 空间资源绑定领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class SpaceResourceBoundEvent extends DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final Long spaceId;
    private final String spaceCode;
    private final Long tenantId;
    private final SpaceResource.ResourceType resourceType;
    private final Long resourceId;
    private final String resourceCode;
    private final SpaceResource.RelationType relationType;
    private final LocalDateTime boundAt;
    private final Long boundBy;

    public SpaceResourceBoundEvent(
            Long spaceId,
            String spaceCode,
            Long tenantId,
            SpaceResource.ResourceType resourceType,
            Long resourceId,
            String resourceCode,
            SpaceResource.RelationType relationType,
            LocalDateTime boundAt,
            Long boundBy) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.spaceId = spaceId;
        this.spaceCode = spaceCode;
        this.tenantId = tenantId;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.resourceCode = resourceCode;
        this.relationType = relationType;
        this.boundAt = boundAt;
        this.boundBy = boundBy;
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
        return "SpaceResourceBound";
    }
}
