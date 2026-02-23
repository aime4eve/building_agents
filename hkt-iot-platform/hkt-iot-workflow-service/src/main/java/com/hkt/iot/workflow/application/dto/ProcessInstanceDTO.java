package com.hkt.iot.workflow.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 流程实例 DTO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class ProcessInstanceDTO {
    String processInstanceId;
    String businessKey;
    String processDefinitionKey;
    String state;
    String tenantId;
    String startedBy;
    String currentActivityId;
    LocalDateTime startedAt;
    LocalDateTime endedAt;
    LocalDateTime updatedAt;
    Map<String, Object> variables;
}
