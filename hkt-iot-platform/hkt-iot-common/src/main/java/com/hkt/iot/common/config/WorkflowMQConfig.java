package com.hkt.iot.common.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工作流消息队列配置
 * 配置工作流事件相关的Exchange、Queue和Binding
 *
 * @author HKT IoT Team
 */
@Configuration
public class WorkflowMQConfig {

    @Value("${rabbitmq.exchange.workflow:workflow.exchange}")
    private String workflowExchange;

    @Value("${rabbitmq.queue.workflow.process.started:workflow.process.started}")
    private String processStartedQueue;

    @Value("${rabbitmq.queue.workflow.process.completed:workflow.process.completed}")
    private String processCompletedQueue;

    @Value("${rabbitmq.queue.workflow.task.completed:workflow.task.completed}")
    private String taskCompletedQueue;

    @Value("${rabbitmq.queue.workflow.sla.warning:workflow.sla.warning}")
    private String slaWarningQueue;

    @Value("${rabbitmq.queue.workflow.sla.breached:workflow.sla.breached}")
    private String slaBreachedQueue;

    /**
     * 工作流交换机
     */
    @Bean
    public TopicExchange workflowExchange() {
        return new TopicExchange(workflowExchange, true, false);
    }

    /**
     * 流程启动队列
     */
    @Bean
    public Queue processStartedQueue() {
        return QueueBuilder.durable(processStartedQueue).build();
    }

    /**
     * 流程完成队列
     */
    @Bean
    public Queue processCompletedQueue() {
        return QueueBuilder.durable(processCompletedQueue).build();
    }

    /**
     * 任务完成队列
     */
    @Bean
    public Queue taskCompletedQueue() {
        return QueueBuilder.durable(taskCompletedQueue).build();
    }

    /**
     * SLA预警队列
     */
    @Bean
    public Queue slaWarningQueue() {
        return QueueBuilder.durable(slaWarningQueue).build();
    }

    /**
     * SLA超时队列
     */
    @Bean
    public Queue slaBreachedQueue() {
        return QueueBuilder.durable(slaBreachedQueue).build();
    }

    /**
     * 绑定关系
     */
    @Bean
    public Binding processStartedBinding() {
        return BindingBuilder.bind(processStartedQueue())
                .to(workflowExchange())
                .with("process.started");
    }

    @Bean
    public Binding processCompletedBinding() {
        return BindingBuilder.bind(processCompletedQueue())
                .to(workflowExchange())
                .with("process.completed");
    }

    @Bean
    public Binding taskCompletedBinding() {
        return BindingBuilder.bind(taskCompletedQueue())
                .to(workflowExchange())
                .with("task.completed");
    }

    @Bean
    public Binding slaWarningBinding() {
        return BindingBuilder.bind(slaWarningQueue())
                .to(workflowExchange())
                .with("sla.warning");
    }

    @Bean
    public Binding slaBreachedBinding() {
        return BindingBuilder.bind(slaBreachedQueue())
                .to(workflowExchange())
                .with("sla.breached");
    }
}
