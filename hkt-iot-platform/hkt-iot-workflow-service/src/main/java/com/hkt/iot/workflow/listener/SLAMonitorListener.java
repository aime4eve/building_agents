package com.hkt.iot.workflow.listener;

import com.hkt.iot.workflow.service.SLAMonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SLA监控监听器
 * 用于启动SLA计时监控
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
public class SLAMonitorListener implements ExecutionListener {

    @Autowired
    private SLAMonitoringService slaMonitoringService;

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        String processInstanceId = execution.getProcessInstanceId();
        String processDefinitionKey = execution.getProcessDefinitionKey();
        String tenantId = (String) execution.getVariable("tenantId");

        if (tenantId == null || tenantId.isEmpty()) {
            log.warn("SLAMonitorListener: tenantId is null or empty for process instance {}", processInstanceId);
            return;
        }

        // 获取SLA配置key，默认使用流程定义key
        String slaConfigKey = (String) execution.getVariable("slaConfigKey");
        if (slaConfigKey == null || slaConfigKey.isEmpty()) {
            slaConfigKey = processDefinitionKey;
        }

        // 获取优先级
        String priority = (String) execution.getVariable("priority");
        if (priority == null || priority.isEmpty()) {
            priority = "MEDIUM";
        }

        // 获取报修类型（用于物业维修工单）
        String workOrderCategory = (String) execution.getVariable("workOrderCategory");

        log.info("SLAMonitorListener: Starting SLA monitoring for process instance {}, " +
                "tenantId={}, slaConfigKey={}, priority={}, workOrderCategory={}",
                processInstanceId, tenantId, slaConfigKey, priority, workOrderCategory);

        try {
            slaMonitoringService.startSLAClock(processInstanceId, slaConfigKey, tenantId, priority, workOrderCategory);
            execution.setVariable("slaMonitoringStarted", true);
        } catch (Exception e) {
            log.error("SLAMonitorListener: Failed to start SLA monitoring for process instance {}", processInstanceId, e);
            execution.setVariable("slaMonitoringStarted", false);
            execution.setVariable("slaMonitoringError", e.getMessage());
        }
    }
}
