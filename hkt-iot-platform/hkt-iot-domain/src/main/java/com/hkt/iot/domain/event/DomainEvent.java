package com.hkt.iot.domain.event;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件接口
 * 所有领域事件的基础接口
 *
 * @author HKT IoT Team
 */
public interface DomainEvent extends Serializable {

    /**
     * 获取事件ID
     *
     * @return 事件ID
     */
    default String getEventId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取事件发生时间
     *
     * @return 事件发生时间
     */
    default Instant getOccurredAt() {
        return Instant.now();
    }

    /**
     * 获取聚合根ID
     *
     * @return 聚合根ID
     */
    String getAggregateId();

    /**
     * 获取聚合根类型
     *
     * @return 聚合根类型
     */
    String getAggregateType();

    /**
     * 获取事件类型
     *
     * @return 事件类型
     */
    String getEventType();
}
