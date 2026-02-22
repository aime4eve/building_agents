package com.hkt.iot.domain.event;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * 领域事件抽象基类
 * 提供领域事件的通用实现
 *
 * @author HKT IoT Team
 */
@Getter
public abstract class AbstractDomainEvent implements DomainEvent {

    /**
     * 事件ID
     */
    private final String eventId;

    /**
     * 事件发生时间
     */
    private final Instant occurredAt;

    /**
     * 聚合根ID
     */
    private final String aggregateId;

    /**
     * 聚合根类型
     */
    private final String aggregateType;

    protected AbstractDomainEvent(String aggregateId, String aggregateType) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractDomainEvent that = (AbstractDomainEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
}
