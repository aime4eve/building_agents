package com.hkt.iot.workflow.interfaces.feign;

import lombok.Data;

import java.util.Map;

/**
 * 规则评估请求
 *
 * @author HKT IoT Team
 */
@Data
public class RuleEvaluationRequest {
    String tenantId;
    String ruleSetKey;
    String ruleSetName;
    Map<String, Object> facts;
    Map<String, Object> context;
}
