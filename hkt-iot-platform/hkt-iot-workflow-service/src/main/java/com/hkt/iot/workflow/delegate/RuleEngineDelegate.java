package com.hkt.iot.workflow.delegate;

import com.hkt.iot.common.workflow.RuleEngineApi;
import com.hkt.iot.workflow.exception.SystemException;
import com.hkt.iot.workflow.exception.WorkflowErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 调用规则引擎的Delegate
 * 用于在工作流中触发规则执行
 *
 * @author HKT IoT Team
 */
@Component
@Scope("prototype")
@Slf4j
public class RuleEngineDelegate implements JavaDelegate {

    @Autowired
    private RuleEngineApi ruleEngineApi;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            // 获取流程变量
            String ruleCode = (String) execution.getVariable("ruleCode");
            String tenantId = execution.getVariable("tenantId") != null 
                ? execution.getVariable("tenantId").toString() 
                : null;
            
            if (ruleCode == null || ruleCode.isEmpty()) {
                throw new SystemException(WorkflowErrorCode.INVALID_PARAMETER, "ruleCode is required");
            }

            log.info("Executing rule: code={}, tenantId={}, processKey={}", 
                ruleCode, tenantId, execution.getProcessBusinessKey());

            // 调用规则引擎
            Map<String, Object> contextVariables = execution.getVariables();
            Map<String, Object> result = ruleEngineApi.executeRule(tenantId, ruleCode, contextVariables);

            // 将执行结果存回流程变量
            if (result != null) {
                execution.setVariables(result);
                execution.setVariable("ruleExecutionResult", result);
            }

            log.info("Rule executed successfully: code={}, result={}", ruleCode, result);

        } catch (Exception e) {
            log.error("Rule execution failed", e);
            throw new BpmnError("RULE_EXECUTION_ERROR", 
                "Failed to execute rule: " + e.getMessage());
        }
    }
}
