package com.hkt.iot.workflow.infrastructure.camunda;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Camunda 流程服务
 *
 * @author HKT IoT Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CamundaProcessService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;

    /**
     * 启动流程实例
     */
    @Transactional
    public ProcessInstance startProcess(
            String processDefinitionKey,
            String businessKey,
            Map<String, Object> variables) {
        log.info("Starting process: processDefinitionKey={}, businessKey={}", processDefinitionKey, businessKey);
        return runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
    }

    /**
     * 挂起流程
     */
    @Transactional
    public void suspendProcess(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
        log.info("Suspended process: processInstanceId={}", processInstanceId);
    }

    /**
     * 激活流程
     */
    @Transactional
    public void activateProcess(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
        log.info("Activated process: processInstanceId={}", processInstanceId);
    }

    /**
     * 取消流程
     */
    @Transactional
    public void cancelProcess(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        log.info("Cancelled process: processInstanceId={}, reason={}", processInstanceId, reason);
    }

    /**
     * 完成任务
     */
    @Transactional
    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
        log.info("Completed task: taskId={}", taskId);
    }

    /**
     * 查询当前活动 ID
     */
    public String getCurrentActivityId(String processInstanceId) {
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        return task != null ? task.getTaskDefinitionKey() : null;
    }

    /**
     * 查询流程实例
     */
    public ProcessInstance getProcessInstance(String processInstanceId) {
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }

    /**
     * 查询历史流程实例
     */
    public HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }

    /**
     * 获取流程变量
     */
    public Map<String, Object> getVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }

    /**
     * 设置流程变量
     */
    @Transactional
    public void setVariables(String processInstanceId, Map<String, Object> variables) {
        runtimeService.setVariables(processInstanceId, variables);
    }
}
