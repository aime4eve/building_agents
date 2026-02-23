package com.hkt.iot.workflow.interfaces.dto;

import lombok.Data;

import java.util.Map;

/**
 * 完成任务请求
 *
 * @author HKT IoT Team
 */
@Data
public class CompleteTaskRequest {
    String userId;
    String comment;
    Map<String, Object> variables;
}
