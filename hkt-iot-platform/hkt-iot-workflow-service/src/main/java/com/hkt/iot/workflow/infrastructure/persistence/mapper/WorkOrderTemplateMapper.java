package com.hkt.iot.workflow.infrastructure.persistence.mapper;

import com.hkt.iot.workflow.domain.model.entity.WorkOrderTemplate;
import com.hkt.iot.workflow.domain.model.valueobject.TenantId;
import com.hkt.iot.workflow.domain.model.valueobject.WorkOrderType;
import com.hkt.iot.workflow.infrastructure.persistence.po.WorkOrderTemplatePO;
import org.springframework.stereotype.Component;

/**
 * 工单模板领域对象与 PO 对象映射器
 *
 * @author HKT IoT Team
 */
@Component
public class WorkOrderTemplateMapper {

    public WorkOrderTemplatePO toPO(WorkOrderTemplate domain) {
        return WorkOrderTemplatePO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .type(domain.getType().name())
                .processDefinitionKey(domain.getProcessDefinitionKey())
                .customFields(domain.getCustomFields())
                .tenantId(domain.getTenantId().getValue())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .deleted(false)
                .build();
    }

    public WorkOrderTemplate toDomain(WorkOrderTemplatePO po) {
        WorkOrderTemplate template = new WorkOrderTemplate();
        template.id = po.getId();
        template.name = po.getName();
        template.type = WorkOrderType.valueOf(po.getType());
        template.processDefinitionKey = po.getProcessDefinitionKey();
        template.customFields = po.getCustomFields();
        template.tenantId = TenantId.of(po.getTenantId());
        template.createdAt = po.getCreatedAt();
        template.updatedAt = po.getUpdatedAt();
        return template;
    }
}
