package com.hkt.iot.workflow.interfaces.feign;

import com.hkt.iot.workflow.interfaces.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 工作流 Feign Client 降级处理
 *
 * @author HKT IoT Team
 */
@Component
@Slf4j
public class WorkflowFeignClientFallback implements WorkflowFeignClient {

    @Override
    public ProcessInstanceResponse startProcess(StartProcessRequest request) {
        log.error("工作流服务调用失败：fallback for startProcess");
        return null;
    }

    @Override
    public ProcessInstanceStatusResponse getProcessStatus(String processInstanceId) {
        log.error("工作流服务调用失败：fallback for getProcessStatus");
        return null;
    }

    @Override
    public CompleteTaskResponse completeTask(String taskId, CompleteTaskRequest request) {
        log.error("工作流服务调用失败：fallback for completeTask");
        return null;
    }

    @Override
    public List<TaskResponse> getPendingTasks(String userId) {
        log.error("工作流服务调用失败：fallback for getPendingTasks");
        return Collections.emptyList();
    }

    @Override
    public RuleEvaluationResponse evaluateRule(RuleEvaluationRequest request) {
        log.error("工作流服务调用失败：fallback for evaluateRule");
        return null;
    }
}
