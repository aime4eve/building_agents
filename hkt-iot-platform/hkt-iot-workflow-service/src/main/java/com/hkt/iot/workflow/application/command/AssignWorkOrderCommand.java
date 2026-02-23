package com.hkt.iot.workflow.application.command;

import lombok.Builder;
import lombok.Data;

/**
 * 分配工单命令
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class AssignWorkOrderCommand {
    String workOrderId;
    String assigneeId;
    String assignedBy;
}
