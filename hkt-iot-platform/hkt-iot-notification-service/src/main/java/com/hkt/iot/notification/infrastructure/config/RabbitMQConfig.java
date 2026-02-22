package com.hkt.iot.notification.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置
 *
 * @author HKT IoT Team
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 通知发送交换机
     */
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    /**
     * 通知重试交换机
     */
    public static final String NOTIFICATION_RETRY_EXCHANGE = "notification.retry.exchange";

    /**
     * 领域事件交换机
     */
    public static final String DOMAIN_EVENT_EXCHANGE = "domain.event.exchange";

    /**
     * 通知发送队列
     */
    public static final String NOTIFICATION_SEND_QUEUE = "notification.send.queue";

    /**
     * 通知重试队列
     */
    public static final String NOTIFICATION_RETRY_QUEUE = "notification.retry.queue";

    /**
     * 告警通知队列
     */
    public static final String NOTIFICATION_ALARM_QUEUE = "notification.alarm.queue";

    /**
     * 通知发送死信队列
     */
    public static final String NOTIFICATION_DLQ_QUEUE = "notification.dlq.queue";

    /**
     * JSON消息转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    /**
     * 通知发送交换机
     */
    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    /**
     * 通知重试交换机
     */
    @Bean
    public DirectExchange notificationRetryExchange() {
        return new DirectExchange(NOTIFICATION_RETRY_EXCHANGE, true, false);
    }

    /**
     * 领域事件交换机
     */
    @Bean
    public TopicExchange domainEventExchange() {
        return new TopicExchange(DOMAIN_EVENT_EXCHANGE, true, false);
    }

    /**
     * 通知发送队列
     */
    @Bean
    public Queue notificationSendQueue() {
        return QueueBuilder.durable(NOTIFICATION_SEND_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_RETRY_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "notification.retry")
                .build();
    }

    /**
     * 通知重试队列
     */
    @Bean
    public Queue notificationRetryQueue() {
        return QueueBuilder.durable(NOTIFICATION_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLQ_QUEUE)
                .build();
    }

    /**
     * 告警通知队列
     */
    @Bean
    public Queue notificationAlarmQueue() {
        return QueueBuilder.durable(NOTIFICATION_ALARM_QUEUE).build();
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue notificationDlqQueue() {
        return QueueBuilder.durable(NOTIFICATION_DLQ_QUEUE).build();
    }

    /**
     * 绑定通知发送队列到交换机
     */
    @Bean
    public Binding notificationSendBinding() {
        return BindingBuilder.bind(notificationSendQueue())
                .to(notificationExchange())
                .with("notification.send");
    }

    /**
     * 绑定通知重试队列到交换机
     */
    @Bean
    public Binding notificationRetryBinding() {
        return BindingBuilder.bind(notificationRetryQueue())
                .to(notificationRetryExchange())
                .with("notification.retry");
    }

    /**
     * 绑定告警通知队列到交换机
     */
    @Bean
    public Binding notificationAlarmBinding() {
        return BindingBuilder.bind(notificationAlarmQueue())
                .to(domainEventExchange())
                .with("alarm.triggered");
    }

    /**
     * 绑定通知成功事件到交换机
     */
    @Bean
    public Binding notificationSentBinding() {
        return BindingBuilder.bind(new Queue("notification.sent.queue", true))
                .to(domainEventExchange())
                .with("notification.sent");
    }

    /**
     * 绑定通知失败事件到交换机
     */
    @Bean
    public Binding notificationFailedBinding() {
        return BindingBuilder.bind(new Queue("notification.failed.queue", true))
                .to(domainEventExchange())
                .with("notification.failed");
    }

    /**
     * 绑定通知请求事件到交换机
     */
    @Bean
    public Binding notificationRequestedBinding() {
        return BindingBuilder.bind(new Queue("notification.requested.queue", true))
                .to(domainEventExchange())
                .with("notification.requested");
    }
}
