package com.hkt.iot.common.workflow;

import com.hkt.iot.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流引擎API
 * 提供给其他服务调用的Feign接口
 *
 * @author HKT IoT Team
 */
@FeignClient(name = "hkt-iot-workflow-service", path = "/api/v1/workflow")
public interface WorkflowEngineApi {

    /**
     * 启动流程实例
     *
     * @param request 启动流程请求
     * @return 流程实例响应
     */
    @PostMapping("/process/start")
    Result<ProcessInstanceResponse> startProcess(@RequestBody StartProcessRequest request);

    /**
     * 查询流程实例状态
     *
     * @param processInstanceId 流程实例ID
     * @return 流程实例状态响应
     */
    @GetMapping("/process/{processInstanceId}/status")
    Result<ProcessInstanceStatusResponse> getProcessStatus(@PathVariable("processInstanceId") String processInstanceId);

    /**
     * 完成用户任务
     *
     * @param taskId    任务ID
     * @param request   完成任务请求
     * @return 完成任务响应
     */
    @PostMapping("/task/{taskId}/complete")
    Result<CompleteTaskResponse> completeTask(
            @PathVariable("taskId") String taskId,
            @RequestBody CompleteTaskRequest request
    );

    /**
     * 查询用户待办任务
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 任务列表响应
     */
    @GetMapping("/task/pending")
    Result<TaskListResponse> getPendingTasks(
            @RequestParam("userId") String userId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    );

    /**
     * 调用规则引擎
     *
     * @param request 规则评估请求
     * @return 规则评估响应
     */
    @PostMapping("/rule/evaluate")
    Result<RuleEvaluationResponse> evaluateRule(@RequestBody RuleEvaluationRequest request);

    // ========== 请求/响应对象定义 ==========

    /**
     * 启动流程请求
     */
    record StartProcessRequest(
            String tenantId,
            String processDefinitionKey,
            String businessKey,
            Map<String, Object> variables
    ) {}

    /**
     * 流程实例响应
     */
    record ProcessInstanceResponse(
            String processInstanceId,
            String businessKey,
            String definitionId,
            Long startedAt,
            String currentActivityId
    ) {}

    /**
     * 流程实例状态响应
     */
    record ProcessInstanceStatusResponse(
            String processInstanceId,
            String businessKey,
            String processDefinitionKey,
            String state,
            CurrentActivityInfo currentActivity,
            Long startedAt,
            SLAInfo slaInfo
    ) {
        record CurrentActivityInfo(
                String id,
                String name,
                String type
        ) {}

        record SLAInfo(
                Long responseDeadline,
                Long resolutionDeadline,
                String responseStatus,
                String resolutionStatus
        ) {}
    }

    /**
     * 完成任务请求
     */
    record CompleteTaskRequest(
            String userId,
            String comment,
            Map<String, Object> variables
    ) {}

    /**
     * 完成任务响应
     */
    record CompleteTaskResponse(
            String taskId,
            String processInstanceId,
            NextActivityInfo nextActivity
    ) {
        record NextActivityInfo(
                String id,
                String name,
                String assignee
        ) {}
    }

    /**
     * 任务列表响应
     */
    record TaskListResponse(
            Long total,
            List<TaskInfo> items
    ) {
        record TaskInfo(
                String taskId,
                String taskName,
                String processInstanceId,
                String businessKey,
                String priority,
                Long createdAt,
                Long slaDeadline,
                String slaStatus
        ) {}
    }

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
