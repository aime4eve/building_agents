package com.hkt.iot.space.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 空间添加到分组领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class SpaceAddedToGroupEvent extends DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final Long groupId;
    private final String groupCode;
    private final String groupName;
    private final Long tenantId;
    private final Long spaceId;
    private final String spaceCode;
    private final String spaceName;
    private final Space.SpaceType spaceType;
    private final LocalDateTime addedAt;
    private final Long addedBy;

    public SpaceAddedToGroupEvent(
            Long groupId,
            String groupCode,
            String groupName,
            Long tenantId,
            Long spaceId,
            String spaceCode,
            String spaceName,
            Space.SpaceType spaceType,
            LocalDateTime addedAt,
            Long addedBy) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.groupId = groupId;
        this.groupCode = groupCode;
        this.groupName = groupName;
        this.tenantId = tenantId;
        this.spaceId = spaceId;
        this.spaceCode = spaceCode;
        this.spaceName = spaceName;
        this.spaceType = spaceType;
        this.addedAt = addedAt;
        this.addedBy = addedBy;
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
        return String.valueOf(groupId);
    }

    @Override
    public String getAggregateType() {
        return "LogicalSpaceGroup";
    }

    @Override
    public String eventType() {
        return "SpaceAddedToGroup";
    }
}
