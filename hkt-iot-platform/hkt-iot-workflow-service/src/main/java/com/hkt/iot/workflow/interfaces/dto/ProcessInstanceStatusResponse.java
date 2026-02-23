package com.hkt.iot.workflow.interfaces.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 流程实例状态响应
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class ProcessInstanceStatusResponse {
    String processInstanceId;
    String businessKey;
    String processDefinitionKey;
    String state;
    CurrentActivity currentActivity;
    String startedAt;
    SLAInfo slaInfo;

    @Data
    @Builder
    public static class CurrentActivity {
        String id;
        String name;
        String type;
    }

    @Data
    @Builder
    public static class SLAInfo {
        String responseDeadline;
        String resolutionDeadline;
        String responseStatus;
        String resolutionStatus;
    }
}
