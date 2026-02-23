package com.hkt.iot.workflow.infrastructure.persistence.mapper;

import com.hkt.iot.workflow.domain.model.aggregate.WorkflowDefinition;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.infrastructure.persistence.po.WorkflowDefinitionPO;
import org.springframework.stereotype.Component;

/**
 * 流程定义领域对象与 PO 对象映射器
 *
 * @author HKT IoT Team
 */
@Component
public class WorkflowDefinitionMapper {

    public WorkflowDefinitionPO toPO(WorkflowDefinition domain) {
        return WorkflowDefinitionPO.builder()
                .id(domain.getId().getValue())
                .definitionKey(domain.getKey().getValue())
                .name(domain.getName())
                .version(domain.getVersion())
                .status(domain.getStatus().name())
                .description(domain.getDescription())
                .tenantId(domain.getTenantId().getValue())
                .createdBy(domain.getCreatedBy().getValue())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .publishedAt(domain.getPublishedAt())
                .versionLock(domain.getVersionLock())
                .deleted(false)
                .build();
    }

    public WorkflowDefinition toDomain(WorkflowDefinitionPO po) {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.id = WorkflowDefinitionId.of(po.getId());
        definition.key = WorkflowDefinitionKey.of(po.getDefinitionKey());
        definition.name = po.getName();
        definition.version = po.getVersion();
        definition.status = WorkflowDefinitionStatus.valueOf(po.getStatus());
        definition.description = po.getDescription();
        definition.tenantId = TenantId.of(po.getTenantId());
        definition.createdBy = UserId.of(po.getCreatedBy());
        definition.createdAt = po.getCreatedAt();
        definition.updatedAt = po.getUpdatedAt();
        definition.publishedAt = po.getPublishedAt();
        definition.versionLock = po.getVersionLock();
        return definition;
    }
}
