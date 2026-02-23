package com.hkt.iot.workflow.infrastructure.messaging;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 领域事件发布器
 * 将领域事件发布到消息队列
 *
 * @author HKT IoT Team
 */
@Component
@Slf4j
public class DomainEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public DomainEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发布领域事件
     */
    public void publish(DomainEvent event) {
        String exchange = "workflow.events";
        String routingKey = "workflow." + event.getEventType();

        log.info("Publishing domain event: eventType={}, aggregateId={}",
                event.getEventType(), event.getAggregateId());

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
        } catch (Exception e) {
            log.error("Failed to publish domain event: eventType={}, error={}",
                    event.getEventType(), e.getMessage(), e);
            // 可以选择将事件存入事件存储表，稍后重试
        }
    }
}
