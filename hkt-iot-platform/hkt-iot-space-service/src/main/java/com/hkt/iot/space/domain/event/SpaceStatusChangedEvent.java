package com.hkt.iot.space.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 空间状态变更领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class SpaceStatusChangedEvent extends DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final Long spaceId;
    private final String spaceCode;
    private final Long tenantId;
    private final Space.SpaceStatus previousStatus;
    private final Space.SpaceStatus currentStatus;
    private final String statusChangeReason;
    private final LocalDateTime changedAt;

    public SpaceStatusChangedEvent(
            Long spaceId,
            String spaceCode,
            Long tenantId,
            Space.SpaceStatus previousStatus,
            Space.SpaceStatus currentStatus,
            String statusChangeReason,
            LocalDateTime changedAt) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.spaceId = spaceId;
        this.spaceCode = spaceCode;
        this.tenantId = tenantId;
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
        this.statusChangeReason = statusChangeReason;
        this.changedAt = changedAt;
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
        return "SpaceStatusChanged";
    }
}
