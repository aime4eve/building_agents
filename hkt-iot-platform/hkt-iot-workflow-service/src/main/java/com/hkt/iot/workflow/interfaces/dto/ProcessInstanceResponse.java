package com.hkt.iot.workflow.interfaces.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 启动流程响应
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class ProcessInstanceResponse {
    String processInstanceId;
    String businessKey;
    String definitionId;
    String startedAt;
    String currentActivityId;
}
