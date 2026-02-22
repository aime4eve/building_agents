package com.hkt.iot.common.workflow.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * 通用异常处理委托类
 * 处理工作流执行过程中的异常
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
public class ExceptionHandlerDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        String businessKey = execution.getBusinessKey();
        String errorMessage = (String) execution.getVariable("errorMessage");
        String errorType = (String) execution.getVariable("errorType");

        log.error("工作流执行异常: processInstanceId={}, businessKey={}, errorType={}, errorMessage={}",
                processInstanceId, businessKey, errorType, errorMessage);

        try {
            // 1. 记录异常信息
            execution.setVariable("errorHandledAt", System.currentTimeMillis());
            execution.setVariable("errorHandled", true);

            // 2. 根据错误类型进行不同处理
            handleExceptionByType(execution, errorType);

            // 3. 发布异常处理事件
            publishExceptionHandledEvent(execution);

        } catch (Exception e) {
            log.error("处理工作流异常失败", e);
            throw new RuntimeException("无法处理工作流异常", e);
        }
    }

    /**
     * 根据错误类型进行不同处理
     */
    private void handleExceptionByType(DelegateExecution execution, String errorType) {
        switch (errorType) {
            case "RULE_ENGINE_ERROR":
                // 规则引擎异常：设置为手动处理
                execution.setVariable("manualInterventionRequired", true);
                execution.setVariable("manualInterventionReason", "规则引擎评估失败，需要人工干预");
                break;

            case "EXTERNAL_SERVICE_ERROR":
                // 外部服务异常：重试或跳过
                Integer retryCount = (Integer) execution.getVariable("retryCount");
                if (retryCount == null) {
                    retryCount = 0;
                }
                execution.setVariable("retryCount", retryCount + 1);
                if (retryCount >= 3) {
                    execution.setVariable("skipExternalService", true);
                }
                break;

            case "VALIDATION_ERROR":
                // 验证异常：终止流程
                execution.setVariable("processTerminated", true);
                execution.setVariable("terminationReason", "数据验证失败");
                break;

            default:
                // 未知异常：记录并继续
                execution.setVariable("unknownError", true);
                break;
        }
    }

    /**
     * 发布异常处理事件
     */
    private void publishExceptionHandledEvent(DelegateExecution execution) {
        // TODO: 发布到消息队列，通知相关服务
        log.info("发布异常处理事件: processInstanceId={}", execution.getProcessInstanceId());
    }
}
