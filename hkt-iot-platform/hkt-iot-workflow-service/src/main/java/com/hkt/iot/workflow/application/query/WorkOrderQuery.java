package com.hkt.iot.workflow.application.query;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单查询对象
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class WorkOrderQuery {
    String tenantId;
    String workOrderNo;
    String title;
    String type;
    String status;
    String priority;
    String assigneeId;
    String reporterId;
    String spaceId;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Boolean overdue;
    Integer page;
    Integer size;
}
