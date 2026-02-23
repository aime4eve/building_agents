package com.hkt.iot.workflow.domain.model.entity;

import com.hkt.iot.domain.model.Entity;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 流程变量实体
 */
@Getter
@NoArgsConstructor
public class ProcessVariable extends Entity<String> {

    private ProcessVariableId id;
    private ProcessInstanceId processInstanceId;
    private String name;
    private String value;
    private ProcessVariableType type;
    private TenantId tenantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProcessVariable create(
            ProcessInstanceId processInstanceId,
            String name,
            String value,
            ProcessVariableType type,
            TenantId tenantId) {
        ProcessVariable variable = new ProcessVariable();
        variable.id = ProcessVariableId.generate();
        variable.processInstanceId = processInstanceId;
        variable.name = name;
        variable.value = value;
        variable.type = type;
        variable.tenantId = tenantId;
        variable.createdAt = LocalDateTime.now();
        variable.updatedAt = LocalDateTime.now();
        return variable;
    }

    public void updateValue(String newValue) {
        this.value = newValue;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String getId() {
        return id != null ? id.getValue() : null;
    }
}
