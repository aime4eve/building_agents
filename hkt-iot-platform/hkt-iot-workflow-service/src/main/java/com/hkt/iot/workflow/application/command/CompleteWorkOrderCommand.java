package com.hkt.iot.workflow.application.command;

import lombok.Builder;
import lombok.Data;

/**
 * 完成工单命令
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class CompleteWorkOrderCommand {
    String workOrderId;
    String handlerId;
    String result;
}
