package com.hkt.iot.space.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 空间删除领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class SpaceDeletedEvent extends DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final Long spaceId;
    private final String spaceCode;
    private final String spaceName;
    private final Long tenantId;
    private final Space.SpaceType spaceType;
    private final LocalDateTime deletedAt;
    private final Long deletedBy;

    public SpaceDeletedEvent(
            Long spaceId,
            String spaceCode,
            String spaceName,
            Long tenantId,
            Space.SpaceType spaceType,
            LocalDateTime deletedAt,
            Long deletedBy) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.spaceId = spaceId;
        this.spaceCode = spaceCode;
        this.spaceName = spaceName;
        this.tenantId = tenantId;
        this.spaceType = spaceType;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
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
        return "SpaceDeleted";
    }
}
