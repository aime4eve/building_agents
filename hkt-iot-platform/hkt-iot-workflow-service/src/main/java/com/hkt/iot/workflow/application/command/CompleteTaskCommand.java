package com.hkt.iot.workflow.application.command;

import lombok.Value;

import java.util.Map;

/**
 * 完成任务命令
 *
 * @author HKT IoT Team
 */
@Value
public class CompleteTaskCommand {
    String taskId;
    String userId;
    String comment;
    Map<String, Object> variables;
}
