package com.hkt.iot.common.workflow.mq;

import com.hkt.iot.common.workflow.event.WorkflowEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 工作流事件消费者
 * 其他服务可以继承此类处理工作流事件
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
public class WorkflowEventConsumer {

    /**
     * 处理流程实例启动事件
     */
    @RabbitListener(queues = "${rabbitmq.queue.workflow.process.started:workflow.process.started}")
    public void handleProcessInstanceStarted(WorkflowEventPublisher.ProcessInstanceStartedEvent event) {
        log.info("收到流程启动事件: processInstanceId={}, businessKey={}, tenantId={}",
                event.processInstanceId(), event.businessKey(), event.tenantId());
        // 子类重写此方法实现具体业务逻辑
    }

    /**
     * 处理流程实例完成事件
     */
    @RabbitListener(queues = "${rabbitmq.queue.workflow.process.completed:workflow.process.completed}")
    public void handleProcessInstanceCompleted(WorkflowEventPublisher.ProcessInstanceCompletedEvent event) {
        log.info("收到流程完成事件: processInstanceId={}, businessKey={}, tenantId={}",
                event.processInstanceId(), event.businessKey(), event.tenantId());
        // 子类重写此方法实现具体业务逻辑
    }

    /**
     * 处理任务完成事件
     */
    @RabbitListener(queues = "${rabbitmq.queue.workflow.task.completed:workflow.task.completed}")
    public void handleTaskCompleted(WorkflowEventPublisher.TaskCompletedEvent event) {
        log.info("收到任务完成事件: taskId={}, processInstanceId={}, tenantId={}",
                event.taskId(), event.processInstanceId(), event.tenantId());
        // 子类重写此方法实现具体业务逻辑
    }

    /**
     * 处理SLA监控预警事件
     */
    @RabbitListener(queues = "${rabbitmq.queue.workflow.sla.warning:workflow.sla.warning}")
    public void handleSLAMonitoringWarning(WorkflowEventPublisher.SLAMonitoringWarningEvent event) {
        log.warn("收到SLA预警事件: monitorId={}, processInstanceId={}, taskId={}, remainingTime={}ms",
                event.monitorId(), event.processInstanceId(), event.taskId(), event.remainingTimeMillis());
        // 子类重写此方法实现具体业务逻辑
        // 例如：发送通知、更新UI等
    }

    /**
     * 处理SLA监控超时事件
     */
    @RabbitListener(queues = "${rabbitmq.queue.workflow.sla.breached:workflow.sla.breached}")
    public void handleSLAMonitoringBreached(WorkflowEventPublisher.SLAMonitoringBreachedEvent event) {
        log.error("收到SLA超时事件: monitorId={}, processInstanceId={}, taskId={}, overtimeDuration={}ms",
                event.monitorId(), event.processInstanceId(), event.taskId(), event.overtimeDurationMillis());
        // 子类重写此方法实现具体业务逻辑
        // 例如：发送告警通知、记录违规等
    }
}
