package com.hkt.iot.workflow.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务 DTO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class TaskDTO {
    String taskId;
    String taskName;
    String taskType;
    String status;
    String processInstanceId;
    String businessKey;
    String assignee;
    String tenantId;
    LocalDateTime createdAt;
    LocalDateTime dueDate;
    String slaStatus;
}
