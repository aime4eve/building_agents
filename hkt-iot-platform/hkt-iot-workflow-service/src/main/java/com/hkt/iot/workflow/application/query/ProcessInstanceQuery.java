package com.hkt.iot.workflow.application.query;

import lombok.Builder;
import lombok.Data;

/**
 * 流程实例查询对象
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class ProcessInstanceQuery {
    String tenantId;
    String processInstanceId;
    String businessKey;
    String processDefinitionKey;
    String state;
    Integer page;
    Integer size;
}
