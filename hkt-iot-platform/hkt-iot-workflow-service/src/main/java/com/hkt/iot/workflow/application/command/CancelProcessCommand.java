package com.hkt.iot.workflow.application.command;

import lombok.Value;

/**
 * 取消流程命令
 *
 * @author HKT IoT Team
 */
@Value
public class CancelProcessCommand {
    String processInstanceId;
    String tenantId;
    String reason;
}
