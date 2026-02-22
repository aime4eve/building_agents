package com.hkt.iot.space.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 逻辑空间分组创建领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class LogicalSpaceGroupCreatedEvent extends DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;
    private final Long groupId;
    private final String groupCode;
    private final String groupName;
    private final Long tenantId;
    private final LogicalSpaceGroup.GroupType groupType;
    private final LocalDateTime createdAt;
    private final Long createdBy;

    public LogicalSpaceGroupCreatedEvent(
            Long groupId,
            String groupCode,
            String groupName,
            Long tenantId,
            LogicalSpaceGroup.GroupType groupType,
            LocalDateTime createdAt,
            Long createdBy) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.groupId = groupId;
        this.groupCode = groupCode;
        this.groupName = groupName;
        this.tenantId = tenantId;
        this.groupType = groupType;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
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
        return "LogicalSpaceGroupCreated";
    }
}
