package com.hkt.iot.workflow.infrastructure.messaging;

import com.hkt.iot.workflow.domain.model.domainevent.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 工作流事件监听器
 * 监听并发布领域事件到通知中心
 *
 * @author HKT IoT Team
 */
@Component
@Slf4j
public class WorkflowEventListener {

    /**
     * 监听流程实例启动事件
     */
    @RabbitListener(queues = "workflow.process.started")
    public void handleProcessInstanceStarted(ProcessInstanceStartedEvent event) {
        log.info("Received ProcessInstanceStartedEvent: processInstanceId={}",
                event.getProcessInstanceId().getValue());
        // TODO: 发送通知或触发其他业务逻辑
    }

    /**
     * 监听流程实例完成事件
     */
    @RabbitListener(queues = "workflow.process.completed")
    public void handleProcessInstanceCompleted(ProcessInstanceCompletedEvent event) {
        log.info("Received ProcessInstanceCompletedEvent: processInstanceId={}",
                event.getProcessInstanceId().getValue());
        // TODO: 发送通知或触发其他业务逻辑
    }

    /**
     * 监听流程实例状态变更事件
     */
    @RabbitListener(queues = "workflow.process.changed")
    public void handleProcessInstanceStateChanged(ProcessInstanceStateChangedEvent event) {
        log.info("Received ProcessInstanceStateChangedEvent: processInstanceId={}, currentState={}",
                event.getProcessInstanceId().getValue(), event.getCurrentState());
        // TODO: 发送通知或触发其他业务逻辑
    }

    /**
     * 监听任务完成事件
     */
    @RabbitListener(queues = "workflow.task.completed")
    public void handleTaskCompleted(TaskCompletedEvent event) {
        log.info("Received TaskCompletedEvent: processInstanceId={}, taskId={}",
                event.getProcessInstanceId().getValue(), event.getTaskId().getValue());
        // TODO: 发送通知或触发其他业务逻辑
    }

    /**
     * 监听 SLA 预警事件
     */
    @RabbitListener(queues = "workflow.sla.warning")
    public void handleSLAWarning(SLAMonitoringWarningEvent event) {
        log.warn("Received SLAMonitoringWarningEvent: monitorId={}, remainingTime={} minutes",
                event.getMonitorId().getValue(), event.getRemainingTime().toMinutes());
        // TODO: 发送预警通知
    }

    /**
     * 监听 SLA 超时事件
     */
    @RabbitListener(queues = "workflow.sla.breached")
    public void handleSLABreached(SLAMonitoringBreachedEvent event) {
        log.error("Received SLAMonitoringBreachedEvent: monitorId={}, overtime={} minutes",
                event.getMonitorId().getValue(), event.getOvertimeDuration().toMinutes());
        // TODO: 发送超时告警
    }
}
