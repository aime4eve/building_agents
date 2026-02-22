package com.hkt.iot.workflow.delegate;

import com.hkt.iot.workflow.exception.SystemException;
import com.hkt.iot.workflow.exception.WorkflowErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动派单Delegate
 * 根据业务规则自动分配工单给处理人
 *
 * @author HKT IoT Team
 */
@Component
@Scope("prototype")
@Slf4j
public class AutoAssignDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            String tenantId = execution.getVariable("tenantId") != null 
                ? execution.getVariable("tenantId").toString() 
                : null;
            String spaceId = execution.getVariable("spaceId") != null 
                ? execution.getVariable("spaceId").toString() 
                : null;
            String priority = execution.getVariable("priority") != null 
                ? execution.getVariable("priority").toString() 
                : "NORMAL";

            log.info("Auto assigning task: tenantId={}, spaceId={}, priority={}", 
                tenantId, spaceId, priority);

            // TODO: 实现自动派单逻辑
            // 1. 查询该空间下的所有可用处理人
            // 2. 根据优先级和技能匹配
            // 3. 根据当前工单负载均衡
            // 4. 返回分配的处理人ID

            // 临时实现：分配给默认处理人
            String assignee = findAssignee(tenantId, spaceId, priority);
            
            execution.setVariable("assignee", assignee);
            execution.setVariable("autoAssignTime", java.time.LocalDateTime.now().toString());

            log.info("Task auto assigned to: {}", assignee);

        } catch (Exception e) {
            log.error("Auto assign failed", e);
            throw new BpmnError("AUTO_ASSIGN_ERROR", 
                "Failed to auto assign: " + e.getMessage());
        }
    }

    /**
     * 查找合适的处理人
     */
    private String findAssignee(String tenantId, String spaceId, String priority) {
        // TODO: 实现真正的派单算法
        // 这里返回默认值
        return "auto-assignee-" + priority.toLowerCase();
    }
}
