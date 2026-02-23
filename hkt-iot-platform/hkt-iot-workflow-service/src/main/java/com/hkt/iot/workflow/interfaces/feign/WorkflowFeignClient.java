package com.hkt.iot.workflow.interfaces.feign;

import com.hkt.iot.workflow.interfaces.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作流引擎 Feign Client
 *
 * @author HKT IoT Team
 */
@FeignClient(name = "workflow-engine", path = "/api/v1/workflow-engine", fallback = WorkflowFeignClientFallback.class)
public interface WorkflowFeignClient {

    /**
     * 启动流程实例
     */
    @PostMapping("/process/start")
    ProcessInstanceResponse startProcess(@RequestBody StartProcessRequest request);

    /**
     * 查询流程实例状态
     */
    @GetMapping("/process/{processInstanceId}/status")
    ProcessInstanceStatusResponse getProcessStatus(@PathVariable String processInstanceId);

    /**
     * 完成用户任务
     */
    @PostMapping("/task/{taskId}/complete")
    CompleteTaskResponse completeTask(
            @PathVariable String taskId,
            @RequestBody CompleteTaskRequest request
    );

    /**
     * 查询用户待办任务
     */
    @GetMapping("/task/pending")
    List<TaskResponse> getPendingTasks(@RequestParam String userId);

    /**
     * 调用规则引擎
     */
    @PostMapping("/rule/evaluate")
    RuleEvaluationResponse evaluateRule(@RequestBody RuleEvaluationRequest request);
}
