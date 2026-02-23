package com.hkt.iot.workflow.application.command;

import lombok.Value;

import java.util.Map;

/**
 * 启动流程命令
 *
 * @author HKT IoT Team
 */
@Value
public class StartProcessCommand {
    String tenantId;
    String processDefinitionKey;
    String businessKey;
    String startedBy;
    Map<String, Object> variables;
}
