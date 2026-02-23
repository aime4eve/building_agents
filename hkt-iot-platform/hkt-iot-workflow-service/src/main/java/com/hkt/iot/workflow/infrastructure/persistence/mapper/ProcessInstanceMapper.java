package com.hkt.iot.workflow.infrastructure.persistence.mapper;

import com.hkt.iot.workflow.domain.model.aggregate.ProcessInstance;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.infrastructure.persistence.po.ProcessInstancePO;
import org.springframework.stereotype.Component;

/**
 * 流程实例领域对象与 PO 对象映射器
 *
 * @author HKT IoT Team
 */
@Component
public class ProcessInstanceMapper {

    public ProcessInstancePO toPO(ProcessInstance domain) {
        return ProcessInstancePO.builder()
                .id(domain.getId().getValue())
                .businessKey(domain.getBusinessKey().getValue())
                .processDefinitionKey(domain.getProcessDefinitionKey().getValue())
                .state(domain.getState().name())
                .tenantId(domain.getTenantId().getValue())
                .startedBy(domain.getStartedBy().getValue())
                .currentActivityId(domain.getCurrentActivityId() != null ?
                        domain.getCurrentActivityId().getValue() : null)
                .startedAt(domain.getStartedAt())
                .endedAt(domain.getEndedAt())
                .updatedAt(domain.getUpdatedAt())
                .version(domain.getVersion())
                .deleted(false)
                .build();
    }

    public ProcessInstance toDomain(ProcessInstancePO po) {
        ProcessInstance instance = new ProcessInstance();
        instance.id = ProcessInstanceId.of(po.getId());
        instance.businessKey = BusinessKey.of(po.getBusinessKey());
        instance.processDefinitionKey = ProcessDefinitionKey.of(po.getProcessDefinitionKey());
        instance.state = ProcessInstanceState.valueOf(po.getState());
        instance.tenantId = TenantId.of(po.getTenantId());
        instance.startedBy = UserId.of(po.getStartedBy());
        instance.currentActivityId = po.getCurrentActivityId() != null ?
                ActivityId.of(po.getCurrentActivityId()) : null;
        instance.startedAt = po.getStartedAt();
        instance.endedAt = po.getEndedAt();
        instance.updatedAt = po.getUpdatedAt();
        instance.version = po.getVersion();
        return instance;
    }
}
