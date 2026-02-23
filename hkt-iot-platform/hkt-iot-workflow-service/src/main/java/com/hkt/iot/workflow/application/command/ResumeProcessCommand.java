package com.hkt.iot.workflow.application.command;

import lombok.Value;

/**
 * 恢复流程命令
 *
 * @author HKT IoT Team
 */
@Value
public class ResumeProcessCommand {
    String processInstanceId;
    String tenantId;
}
