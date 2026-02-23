package com.hkt.iot.workflow.application.command;

import lombok.Builder;
import lombok.Data;

/**
 * 创建工单命令
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class CreateWorkOrderCommand {
    String tenantId;
    String title;
    String description;
    String type;
    String priority;
    String templateId;
    String spaceId;
    String reporterId;
}
