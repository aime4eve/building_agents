package com.hkt.iot.space.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 逻辑空间分组更新领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class LogicalSpaceGroupUpdatedEvent extends DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final Long groupId;
    private final String groupCode;
    private final String groupName;
    private final Long tenantId;
    private final LogicalSpaceGroup.GroupType groupType;
    private final LocalDateTime updatedAt;
    private final Long updatedBy;

    public LogicalSpaceGroupUpdatedEvent(
            Long groupId,
            String groupCode,
            String groupName,
            Long tenantId,
            LogicalSpaceGroup.GroupType groupType,
            LocalDateTime updatedAt,
            Long updatedBy) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.groupId = groupId;
        this.groupCode = groupCode;
        this.groupName = groupName;
        this.tenantId = tenantId;
        this.groupType = groupType;
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
        return String.valueOf(groupId);
    }

    @Override
    public String getAggregateType() {
        return "LogicalSpaceGroup";
    }

    @Override
    public String eventType() {
        return "LogicalSpaceGroupUpdated";
    }
}
