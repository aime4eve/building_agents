package com.hkt.iot.workflow.interfaces.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 任务响应
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class TaskResponse {
    String taskId;
    String taskName;
    String processInstanceId;
    String businessKey;
    String priority;
    String createdAt;
    String slaDeadline;
    String slaStatus;
}
