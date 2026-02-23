package com.hkt.iot.workflow.application.query;

import lombok.Builder;
import lombok.Data;

/**
 * 任务查询对象
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class TaskQuery {
    String tenantId;
    String assignee;
    String processInstanceId;
    String status;
    Integer page;
    Integer size;
}
