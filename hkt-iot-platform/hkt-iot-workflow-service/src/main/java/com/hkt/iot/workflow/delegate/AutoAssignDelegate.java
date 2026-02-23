package com.hkt.iot.workflow.delegate;

import com.hkt.iot.workflow.interfaces.feign.RuleEvaluationRequest;
import com.hkt.iot.workflow.interfaces.feign.RuleEvaluationResponse;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 自动派单委托类 - 调用规则引擎
 * 根据业务规则自动分配工单给处理人
 *
 * @author HKT IoT Team
 */
@Component
@Scope("prototype")
@Slf4j
public class AutoAssignDelegate implements JavaDelegate {

    // TODO: 注入规则引擎 Feign Client
    // private final RuleEngineFeignClient ruleEngineClient;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            // 1. 获取流程变量
            String tenantId = execution.getVariable("tenantId") != null
                    ? execution.getVariable("tenantId").toString()
                    : null;
            String workOrderType = execution.getVariable("workOrderType") != null
                    ? execution.getVariable("workOrderType").toString()
                    : null;
            String spaceId = execution.getVariable("spaceId") != null
                    ? execution.getVariable("spaceId").toString()
                    : null;
            String priority = execution.getVariable("priority") != null
                    ? execution.getVariable("priority").toString()
                    : "NORMAL";

            log.info("Auto assigning task: tenantId={}, workOrderType={}, spaceId={}, priority={}",
                    tenantId, workOrderType, spaceId, priority);

            // 2. 构造规则评估请求
            RuleEvaluationRequest request = new RuleEvaluationRequest();
            request.setTenantId(tenantId);
            request.setRuleSetKey("auto-assign-rules");
            request.setRuleSetName("自动派单规则集");
            request.setFacts(Map.of(
                    "workOrderType", workOrderType,
                    "spaceId", spaceId,
                    "priority", priority
            ));

            // 3. 调用规则引擎 (TODO: 待注入 Feign Client)
            // RuleEvaluationResponse response = ruleEngineClient.evaluateRule(request);
            RuleEvaluationResponse response = mockEvaluateRule(request);

            if (response != null && response.isSuccess()) {
                // 4. 设置流程变量
                String assigneeId = (String) response.getResults().get("assigneeId");
                execution.setVariable("assigneeId", assigneeId);
                execution.setVariable("assignSuccess", true);
                execution.setVariable("assignReason", (String) response.getResults().get("assignReason"));

                log.info("Task auto assigned to: {}, reason: {}", assigneeId, response.getResults().get("assignReason"));
            } else {
                execution.setVariable("assignSuccess", false);
                log.warn("Auto assign failed: {}", response != null ? response.getMessage() : "unknown error");
            }

        } catch (Exception e) {
            log.error("Auto assign failed", e);
            execution.setVariable("assignSuccess", false);
            throw new BpmnError("AUTO_ASSIGN_ERROR",
                    "Failed to auto assign: " + e.getMessage());
        }
    }

    /**
     * 模拟规则引擎评估（临时实现）
     */
    private RuleEvaluationResponse mockEvaluateRule(RuleEvaluationRequest request) {
        RuleEvaluationResponse response = new RuleEvaluationResponse();
        response.setSuccess(true);
        response.setResults(Map.of(
                "assigneeId", "user-" + System.currentTimeMillis() % 100,
                "assignReason", "技能匹配且负载最低"
        ));
        response.setMatchedRules(java.util.List.of("rule-001", "rule-002"));
        return response;
    }
}
