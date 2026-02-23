package com.hkt.iot.workflow.application.dto;

import lombok.Builder;
import lombok.Data;

/**
 * SLA 配置 DTO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class SLAConfigDTO {
    String id;
    String processDefinitionKey;
    String taskDefinitionKey;
    String tenantId;
    Long responseTimeLimit;
    Long resolutionTimeLimit;
    String priority;
}
