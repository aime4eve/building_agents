package com.hkt.iot.workflow.interfaces.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 完成任务响应
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class CompleteTaskResponse {
    String taskId;
    String processInstanceId;
    NextActivity nextActivity;

    @Data
    @Builder
    public static class NextActivity {
        String id;
        String name;
        String assignee;
    }
}
