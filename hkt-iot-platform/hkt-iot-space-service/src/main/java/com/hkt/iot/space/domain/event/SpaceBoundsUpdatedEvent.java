package com.hkt.iot.space.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.space.domain.model.SpatialBounds;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 空间边界更新领域事件
 * 当空间的地理边界被设置或更新时触发此事件
 *
 * @author HKT IoT Team
 */
@Getter
public class SpaceBoundsUpdatedEvent extends DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final Long spaceId;
    private final String spaceCode;
    private final Long tenantId;
    private final SpatialBounds spatialBounds;
    private final LocalDateTime updatedAt;
    private final Long updatedBy;

    public SpaceBoundsUpdatedEvent(
            Long spaceId,
            String spaceCode,
            Long tenantId,
            SpatialBounds spatialBounds,
            LocalDateTime updatedAt,
            Long updatedBy) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.spaceId = spaceId;
        this.spaceCode = spaceCode;
        this.tenantId = tenantId;
        this.spatialBounds = spatialBounds;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
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
        return "SpaceBoundsUpdated";
    }
}
