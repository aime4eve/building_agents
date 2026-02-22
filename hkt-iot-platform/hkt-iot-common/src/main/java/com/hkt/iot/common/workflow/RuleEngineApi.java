package com.hkt.iot.common.workflow;

import com.hkt.iot.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 规则引擎API
 * 工作流引擎通过此接口调用规则引擎
 *
 * @author HKT IoT Team
 */
@FeignClient(name = "hkt-iot-rule-service", path = "/api/v1/rule")
public interface RuleEngineApi {

    /**
     * 评估规则
     *
     * @param request 规则评估请求
     * @return 规则评估响应
     */
    @PostMapping("/evaluate")
    Result<RuleEvaluationResponse> evaluateRule(@RequestBody RuleEvaluationRequest request);

    /**
     * 批量评估规则
     *
     * @param requests 规则评估请求列表
     * @return 规则评估响应列表
     */
    @PostMapping("/evaluate-batch")
    Result<List<RuleEvaluationResponse>> evaluateRuleBatch(@RequestBody List<RuleEvaluationRequest> requests);

    // ========== 请求/响应对象定义 ==========

    /**
     * 规则评估请求
     */
    record RuleEvaluationRequest(
            String tenantId,
            String ruleSetKey,
            String ruleSetName,
            Map<String, Object> facts,
            Map<String, Object> context
    ) {}

    /**
     * 规则评估响应
     */
    record RuleEvaluationResponse(
            String evaluationId,
            boolean success,
            Map<String, Object> results,
            List<String> matchedRules,
            String message
    ) {}
}
