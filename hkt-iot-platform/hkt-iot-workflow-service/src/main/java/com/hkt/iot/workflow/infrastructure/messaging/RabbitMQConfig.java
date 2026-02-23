package com.hkt.iot.workflow.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 *
 * @author HKT IoT Team
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 工作流事件交换机
     */
    @Bean
    public TopicExchange workflowExchange() {
        return new TopicExchange("workflow.events");
    }

    /**
     * 流程实例启动队列
     */
    @Bean
    public Queue processStartedQueue() {
        return new Queue("workflow.process.started");
    }

    /**
     * 流程实例完成队列
     */
    @Bean
    public Queue processCompletedQueue() {
        return new Queue("workflow.process.completed");
    }

    /**
     * 流程实例状态变更队列
     */
    @Bean
    public Queue processChangedQueue() {
        return new Queue("workflow.process.changed");
    }

    /**
     * 任务完成队列
     */
    @Bean
    public Queue taskCompletedQueue() {
        return new Queue("workflow.task.completed");
    }

    /**
     * SLA 预警队列
     */
    @Bean
    public Queue slaWarningQueue() {
        return new Queue("workflow.sla.warning");
    }

    /**
     * SLA 超时队列
     */
    @Bean
    public Queue slaBreachedQueue() {
        return new Queue("workflow.sla.breached");
    }

    /**
     * 绑定流程实例启动队列
     */
    @Bean
    public Binding processStartedBinding(TopicExchange workflowExchange, Queue processStartedQueue) {
        return BindingBuilder.bind(processStartedQueue)
                .to(workflowExchange)
                .with("workflow.ProcessInstanceStarted");
    }

    /**
     * 绑定流程实例完成队列
     */
    @Bean
    public Binding processCompletedBinding(TopicExchange workflowExchange, Queue processCompletedQueue) {
        return BindingBuilder.bind(processCompletedQueue)
                .to(workflowExchange)
                .with("workflow.ProcessInstanceCompleted");
    }

    /**
     * 绑定流程实例状态变更队列
     */
    @Bean
    public Binding processChangedBinding(TopicExchange workflowExchange, Queue processChangedQueue) {
        return BindingBuilder.bind(processChangedQueue)
                .to(workflowExchange)
                .with("workflow.ProcessInstanceStateChanged");
    }

    /**
     * 绑定任务完成队列
     */
    @Bean
    public Binding taskCompletedBinding(TopicExchange workflowExchange, Queue taskCompletedQueue) {
        return BindingBuilder.bind(taskCompletedQueue)
                .to(workflowExchange)
                .with("workflow.TaskCompleted");
    }

    /**
     * 绑定 SLA 预警队列
     */
    @Bean
    public Binding slaWarningBinding(TopicExchange workflowExchange, Queue slaWarningQueue) {
        return BindingBuilder.bind(slaWarningQueue)
                .to(workflowExchange)
                .with("workflow.SLAMonitoringWarning");
    }

    /**
     * 绑定 SLA 超时队列
     */
    @Bean
    public Binding slaBreachedBinding(TopicExchange workflowExchange, Queue slaBreachedQueue) {
        return BindingBuilder.bind(slaBreachedQueue)
                .to(workflowExchange)
                .with("workflow.SLAMonitoringBreached");
    }

    /**
     * JSON 消息转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
