package com.hkt.iot.device.domain.event;

import com.hkt.iot.device.domain.model.OTATask;
import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * OTA任务创建领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class OTATaskCreatedEvent extends DomainEvent {

    private final Long taskId;
    private final String taskCode;
    private final Long tenantId;
    private final OTATask.TaskType taskType;
    private final String targetVersion;
    private final LocalDateTime createdAt;

    public OTATaskCreatedEvent(
            Long taskId,
            String taskCode,
            Long tenantId,
            OTATask.TaskType taskType,
            String targetVersion,
            LocalDateTime createdAt) {
        this.taskId = taskId;
        this.taskCode = taskCode;
        this.tenantId = tenantId;
        this.taskType = taskType;
        this.targetVersion = targetVersion;
        this.createdAt = createdAt;
    }

    @Override
    public String eventType() {
        return "OTATaskCreated";
    }
}
