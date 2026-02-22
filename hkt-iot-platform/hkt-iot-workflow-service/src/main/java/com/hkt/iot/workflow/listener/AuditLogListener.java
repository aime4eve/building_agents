package com.hkt.iot.workflow.listener;

import com.hkt.iot.workflow.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 流程变量审计监听器
 * 用于记录流程变量的变更历史
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
public class AuditLogListener implements ExecutionListener {

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        String processInstanceId = execution.getProcessInstanceId();
        String processDefinitionKey = execution.getProcessDefinitionKey();
        String eventName = execution.getEventName();
        String currentActivityId = execution.getCurrentActivityId();
        String tenantId = (String) execution.getVariable("tenantId");

        if (tenantId == null || tenantId.isEmpty()) {
            log.warn("AuditLogListener: tenantId is null or empty for process instance {}", processInstanceId);
            return;
        }

        log.debug("AuditLogListener: Recording audit log - processInstanceId={}, eventName={}, activityId={}",
                processInstanceId, eventName, currentActivityId);

        try {
            // 获取当前流程变量
            Map<String, Object> variables = execution.getVariables();

            // 过滤敏感信息
            Map<String, Object> filteredVariables = filterSensitiveVariables(variables);

            // 记录审计日志
            AuditLogEntry auditLogEntry = AuditLogEntry.builder()
                    .processInstanceId(processInstanceId)
                    .processDefinitionKey(processDefinitionKey)
                    .eventName(eventName)
                    .activityId(currentActivityId)
                    .tenantId(tenantId)
                    .variables(filteredVariables)
                    .timestamp(System.currentTimeMillis())
                    .build();

            auditLogService.logProcessVariableChange(auditLogEntry);

            // 记录到流程变量中
            @SuppressWarnings("unchecked")
            Map<String, Object> auditTrail = (Map<String, Object>) execution.getVariable("auditTrail");
            if (auditTrail == null) {
                auditTrail = new HashMap<>();
            }
            auditTrail.put(eventName + "_" + System.currentTimeMillis(), auditLogEntry);
            execution.setVariable("auditTrail", auditTrail);

        } catch (Exception e) {
            log.error("AuditLogListener: Failed to record audit log for process instance {}", processInstanceId, e);
            // 不抛出异常，避免影响流程执行
        }
    }

    /**
     * 过滤敏感变量
     */
    private Map<String, Object> filterSensitiveVariables(Map<String, Object> variables) {
        Map<String, Object> filtered = new HashMap<>();
        String[] sensitiveKeys = {"password", "token", "secret", "key", "credential"};

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = entry.getKey().toLowerCase();
            boolean isSensitive = false;
            for (String sensitiveKey : sensitiveKeys) {
                if (key.contains(sensitiveKey)) {
                    isSensitive = true;
                    break;
                }
            }
            if (!isSensitive) {
                filtered.put(entry.getKey(), entry.getValue());
            } else {
                filtered.put(entry.getKey(), "***");
            }
        }
        return filtered;
    }

    /**
     * 审计日志条目
     */
    @lombok.Builder
    @lombok.Data
    public static class AuditLogEntry {
        private String processInstanceId;
        private String processDefinitionKey;
        private String eventName;
        private String activityId;
        private String tenantId;
        private Map<String, Object> variables;
        private Long timestamp;
    }
}
