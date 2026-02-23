package com.hkt.iot.workflow.application.command;

import lombok.Value;

/**
 * 挂起流程命令
 *
 * @author HKT IoT Team
 */
@Value
public class SuspendProcessCommand {
    String processInstanceId;
    String tenantId;
}
