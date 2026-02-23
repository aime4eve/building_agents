package com.hkt.iot.workflow.domain.model.entity;

import com.hkt.iot.domain.model.Entity;
import com.hkt.iot.workflow.domain.model.valueobject.TenantId;
import com.hkt.iot.workflow.domain.model.valueobject.WorkOrderType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 工单模板实体
 *
 * @author HKT IoT Team
 */
@Getter
@NoArgsConstructor
public class WorkOrderTemplate extends Entity<String> {

    private String id;
    private String name;
    private WorkOrderType type;
    private String processDefinitionKey;
    private String customFields;
    private TenantId tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WorkOrderTemplate create(
            String name,
            WorkOrderType type,
            String processDefinitionKey,
            String customFields,
            TenantId tenantId) {
        WorkOrderTemplate template = new WorkOrderTemplate();
        template.id = java.util.UUID.randomUUID().toString().replace("-", "");
        template.name = Objects.requireNonNull(name, "name cannot be null");
        template.type = Objects.requireNonNull(type, "type cannot be null");
        template.processDefinitionKey = processDefinitionKey;
        template.customFields = customFields;
        template.tenantId = Objects.requireNonNull(tenantId, "tenantId cannot be null");
        template.createdAt = LocalDateTime.now();
        template.updatedAt = LocalDateTime.now();
        return template;
    }

    public void update(String name, String customFields) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.customFields = customFields;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String getId() {
        return id;
    }
}
