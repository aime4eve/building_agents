package com.hkt.iot.workflow.interfaces.dto;

import lombok.Data;

import java.util.Map;

/**
 * 启动流程请求
 *
 * @author HKT IoT Team
 */
@Data
public class StartProcessRequest {
    String tenantId;
    String processDefinitionKey;
    String businessKey;
    String startedBy;
    Map<String, Object> variables;
}
