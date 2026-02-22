package com.hkt.iot.common.workflow.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 工作流事件发布器
 * 发布工作流相关的领域事件到消息队列
 *
 * @author HKT IoT Team
 */
@Component
public class WorkflowEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.workflow:workflow.exchange}")
    private String workflowExchange;

    public WorkflowEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发布流程实例启动事件
     */
    public void publishProcessInstanceStarted(
            String processInstanceId,
            String processDefinitionKey,
            String businessKey,
            String tenantId,
            String startedBy,
            Map<String, Object> variables
    ) {
        ProcessInstanceStartedEvent event = new ProcessInstanceStartedEvent(
                processInstanceId,
                processDefinitionKey,
                businessKey,
                tenantId,
                startedBy,
                variables,
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(workflowExchange, "process.started", event);
    }

    /**
     * 发布流程实例完成事件
     */
    public void publishProcessInstanceCompleted(
            String processInstanceId,
            String processDefinitionKey,
            String businessKey,
            String tenantId,
            LocalDateTime startedAt,
            LocalDateTime completedAt
    ) {
        ProcessInstanceCompletedEvent event = new ProcessInstanceCompletedEvent(
                processInstanceId,
                processDefinitionKey,
                businessKey,
                tenantId,
                startedAt,
                completedAt
        );
        rabbitTemplate.convertAndSend(workflowExchange, "process.completed", event);
    }

    /**
     * 发布任务完成事件
     */
    public void publishTaskCompleted(
            String processInstanceId,
            String taskId,
            String tenantId,
            Map<String, Object> variables
    ) {
        TaskCompletedEvent event = new TaskCompletedEvent(
                processInstanceId,
                taskId,
                tenantId,
                variables,
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(workflowExchange, "task.completed", event);
    }

    /**
     * 发布SLA监控预警事件
     */
    public void publishSLAMonitoringWarning(
            String monitorId,
            String processInstanceId,
            String taskId,
            long remainingTimeMillis,
            String tenantId
    ) {
        SLAMonitoringWarningEvent event = new SLAMonitoringWarningEvent(
                monitorId,
                processInstanceId,
                taskId,
                remainingTimeMillis,
                tenantId,
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(workflowExchange, "sla.warning", event);
    }

    /**
     * 发布SLA监控超时事件
     */
    public void publishSLAMonitoringBreached(
            String monitorId,
            String processInstanceId,
            String taskId,
            long overtimeDurationMillis,
            String tenantId
    ) {
        SLAMonitoringBreachedEvent event = new SLAMonitoringBreachedEvent(
                monitorId,
                processInstanceId,
                taskId,
                overtimeDurationMillis,
                tenantId,
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(workflowExchange, "sla.breached", event);
    }

    // ========== 事件定义 ==========

    public record ProcessInstanceStartedEvent(
            String processInstanceId,
            String processDefinitionKey,
            String businessKey,
            String tenantId,
            String startedBy,
            Map<String, Object> variables,
            LocalDateTime occurredAt
    ) {}

    public record ProcessInstanceCompletedEvent(
            String processInstanceId,
            String processDefinitionKey,
            String businessKey,
            String tenantId,
            LocalDateTime startedAt,
            LocalDateTime completedAt
    ) {}

    public record TaskCompletedEvent(
            String processInstanceId,
            String taskId,
            String tenantId,
            Map<String, Object> variables,
            LocalDateTime occurredAt
    ) {}

    public record SLAMonitoringWarningEvent(
            String monitorId,
            String processInstanceId,
            String taskId,
            long remainingTimeMillis,
            String tenantId,
            LocalDateTime occurredAt
    ) {}

    public record SLAMonitoringBreachedEvent(
            String monitorId,
            String processInstanceId,
            String taskId,
            long overtimeDurationMillis,
            String tenantId,
            LocalDateTime occurredAt
    ) {}
}
