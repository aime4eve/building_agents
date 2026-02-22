package com.hkt.iot.common.workflow.delegate;

import com.hkt.iot.common.workflow.RuleEngineApi;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 自动派单委托类
 * 工作流引擎通过此类调用规则引擎进行自动派单
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
public class AutoAssignDelegate implements JavaDelegate {

    private final RuleEngineApi ruleEngineApi;

    public AutoAssignDelegate(RuleEngineApi ruleEngineApi) {
        this.ruleEngineApi = ruleEngineApi;
    }

    @Override
    public void execute(DelegateExecution execution) {
        log.info("执行自动派单委托: processInstanceId={}, businessKey={}",
                execution.getProcessInstanceId(), execution.getBusinessKey());

        try {
            // 1. 获取流程变量
            String tenantId = execution.getTenantId();
            String workOrderType = (String) execution.getVariable("workOrderType");
            String spaceId = (String) execution.getVariable("spaceId");
            String priority = (String) execution.getVariable("priority");

            // 2. 构造规则评估请求
            RuleEngineApi.RuleEvaluationRequest request = new RuleEngineApi.RuleEvaluationRequest(
                    tenantId,
                    "auto-assign-rules",
                    "自动派单规则集",
                    Map.of(
                            "workOrderType", workOrderType,
                            "spaceId", spaceId,
                            "priority", priority
                    ),
                    Map.of(
                            "processInstanceId", execution.getProcessInstanceId(),
                            "businessKey", execution.getBusinessKey()
                    )
            );

            // 3. 调用规则引擎
            var response = ruleEngineApi.evaluateRule(request);

            if (response != null && response.getData() != null && response.getData().success()) {
                RuleEngineApi.RuleEvaluationResponse result = response.getData();

                // 4. 设置流程变量
                execution.setVariable("assigneeId", result.results().get("assigneeId"));
                execution.setVariable("assignSuccess", true);
                execution.setVariable("assignReason", result.results().get("assignReason"));

                log.info("自动派单成功: assigneeId={}, reason={}",
                        result.results().get("assigneeId"), result.results().get("assignReason"));
            } else {
                execution.setVariable("assignSuccess", false);
                execution.setVariable("assignFailureReason", response != null ? response.getData().message() : "规则引擎调用失败");
                log.warn("自动派单失败: {}", response != null ? response.getData() : "无响应");
            }
        } catch (Exception e) {
            log.error("自动派单异常", e);
            execution.setVariable("assignSuccess", false);
            execution.setVariable("assignFailureReason", "规则引擎调用异常: " + e.getMessage());
        }
    }
}
