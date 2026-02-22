package com.hkt.iot.device.domain.event;

import com.hkt.iot.device.domain.model.OTATask;
import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * OTA任务完成领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class OTATaskCompletedEvent extends DomainEvent {

    private final Long taskId;
    private final String taskCode;
    private final Long tenantId;
    private final OTATask.TaskStatus taskStatus;
    private final Integer successCount;
    private final Integer failureCount;
    private final LocalDateTime completedAt;

    public OTATaskCompletedEvent(
            Long taskId,
            String taskCode,
            Long tenantId,
            OTATask.TaskStatus taskStatus,
            Integer successCount,
            Integer failureCount,
            LocalDateTime completedAt) {
        this.taskId = taskId;
        this.taskCode = taskCode;
        this.tenantId = tenantId;
        this.taskStatus = taskStatus;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.completedAt = completedAt;
    }

    @Override
    public String eventType() {
        return "OTATaskCompleted";
    }
}
