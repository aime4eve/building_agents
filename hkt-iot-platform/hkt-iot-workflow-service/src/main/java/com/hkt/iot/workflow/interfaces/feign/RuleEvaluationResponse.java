package com.hkt.iot.workflow.interfaces.feign;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 规则评估响应
 *
 * @author HKT IoT Team
 */
@Data
public class RuleEvaluationResponse {
    String evaluationId;
    boolean success;
    Map<String, Object> results;
    List<String> matchedRules;
    String message;
}
