package com.hkt.iot.workflow.application.command;

import lombok.Value;

/**
 * 分配任务命令
 *
 * @author HKT IoT Team
 */
@Value
public class AssignTaskCommand {
    String taskId;
    String assigneeId;
    String tenantId;
}
