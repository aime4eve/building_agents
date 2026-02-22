package com.hkt.iot.device.application.event;

import com.hkt.iot.device.application.event.kafka.producer.KafkaEventProducer;
import com.hkt.iot.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备领域事件发布器
 * 统一处理设备相关领域事件的发布
 *
 * @author HKT IoT Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceEventPublisher {

    private final KafkaEventProducer kafkaEventProducer;

    /**
     * 发布单个领域事件
     */
    public void publishEvent(DomainEvent event) {
        try {
            kafkaEventProducer.publishEvent(event);
            log.debug("领域事件发布成功: eventType={}", event.eventType());
        } catch (Exception e) {
            log.error("领域事件发布失败: eventType={}, error={}",
                    event.eventType(), e.getMessage(), e);
        }
    }

    /**
     * 发布聚合根的所有领域事件
     */
    public void publishDomainEvents(com.hkt.iot.domain.model.AggregateRoot<?> aggregateRoot) {
        if (aggregateRoot == null || aggregateRoot.getDomainEvents() == null) {
            return;
        }

        for (DomainEvent event : aggregateRoot.getDomainEvents()) {
            publishEvent(event);
        }

        // 清空已发布的事件
        aggregateRoot.clearDomainEvents();
    }
}
