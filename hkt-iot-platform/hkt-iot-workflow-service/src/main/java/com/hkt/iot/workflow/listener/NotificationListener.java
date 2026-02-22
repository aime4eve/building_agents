package com.hkt.iot.workflow.listener;

import com.hkt.iot.workflow.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 通知发送监听器
 * 用于在任务创建/分配/完成时发送通知
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
public class NotificationListener implements TaskListener {

    @Autowired
    private NotificationService notificationService;

    @Override
    public void notify(DelegateTask delegateTask) {
        String taskId = delegateTask.getId();
        String taskName = delegateTask.getName();
        String assignee = delegateTask.getAssignee();
        String eventName = delegateTask.getEventName();
        String tenantId = (String) delegateTask.getVariable("tenantId");

        if (tenantId == null || tenantId.isEmpty()) {
            log.warn("NotificationListener: tenantId is null or empty for task {}", taskId);
            return;
        }

        // 根据事件类型发送不同通知
        switch (eventName) {
            case EVENTNAME_CREATE:
                handleTaskCreated(delegateTask, tenantId, taskId, taskName, assignee);
                break;
            case EVENTNAME_ASSIGNMENT:
                handleTaskAssigned(delegateTask, tenantId, taskId, taskName, assignee);
                break;
            case EVENTNAME_COMPLETE:
                handleTaskCompleted(delegateTask, tenantId, taskId, taskName, assignee);
                break;
            default:
                log.debug("NotificationListener: Unhandled event name {} for task {}", eventName, taskId);
        }
    }

    /**
     * 处理任务创建事件
     */
    private void handleTaskCreated(DelegateTask delegateTask, String tenantId, String taskId, String taskName, String assignee) {
        log.info("NotificationListener: Task created - tenantId={}, taskId={}, taskName={}, assignee={}",
                tenantId, taskId, taskName, assignee);

        if (assignee != null && !assignee.isEmpty()) {
            try {
                notificationService.sendTaskNotification(tenantId, assignee, taskName,
                        "您有一个新的待办任务", delegateTask.getId());
            } catch (Exception e) {
                log.error("NotificationListener: Failed to send task created notification for task {}", taskId, e);
            }
        }
    }

    /**
     * 处理任务分配事件
     */
    private void handleTaskAssigned(DelegateTask delegateTask, String tenantId, String taskId, String taskName, String assignee) {
        log.info("NotificationListener: Task assigned - tenantId={}, taskId={}, taskName={}, assignee={}",
                tenantId, taskId, taskName, assignee);

        if (assignee != null && !assignee.isEmpty()) {
            try {
                notificationService.sendTaskNotification(tenantId, assignee, taskName,
                        "任务已分配给您", delegateTask.getId());
            } catch (Exception e) {
                log.error("NotificationListener: Failed to send task assigned notification for task {}", taskId, e);
            }
        }
    }

    /**
     * 处理任务完成事件
     */
    private void handleTaskCompleted(DelegateTask delegateTask, String tenantId, String taskId, String taskName, String assignee) {
        log.info("NotificationListener: Task completed - tenantId={}, taskId={}, taskName={}, assignee={}",
                tenantId, taskId, taskName, assignee);

        // 任务完成后可能需要通知流程发起人
        String initiator = (String) delegateTask.getVariable("initiator");
        if (initiator != null && !initiator.isEmpty()) {
            try {
                notificationService.sendTaskNotification(tenantId, initiator, taskName,
                        "任务已完成", delegateTask.getId());
            } catch (Exception e) {
                log.error("NotificationListener: Failed to send task completed notification for task {}", taskId, e);
            }
        }
    }
}
