package com.hkt.iot.space.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 空间创建领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class SpaceCreatedEvent extends DomainEvent {

    private final Long spaceId;
    private final String spaceCode;
    private final String spaceName;
    private final Long tenantId;
    private final Space.SpaceType spaceType;
    private final Integer spaceLevel;
    private final LocalDateTime createdAt;

    /**
     * 空间类型枚举
     */
    public enum SpaceType {
        PARK, BUILDING, FLOOR, ROOM
    }

    public SpaceCreatedEvent(
            Long spaceId,
            String spaceCode,
            String spaceName,
            Long tenantId,
            SpaceType spaceType,
            Integer spaceLevel,
            LocalDateTime createdAt) {
        this.spaceId = spaceId;
        this.spaceCode = spaceCode;
        this.spaceName = spaceName;
        this.tenantId = tenantId;
        this.spaceType = spaceType;
        this.spaceLevel = spaceLevel;
        this.createdAt = createdAt;
    }

    @Override
    public String eventType() {
        return "SpaceCreated";
    }
}
