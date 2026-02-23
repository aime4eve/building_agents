package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.UUID;

/**
 * 流程定义 ID - 值对象
 *
 * @author HKT IoT Team
 */
@Value
@EqualsAndHashCode(of = "value")
public class WorkflowDefinitionId {
    String value;

    private WorkflowDefinitionId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("WorkflowDefinitionId cannot be empty");
        }
        this.value = value;
    }

    public static WorkflowDefinitionId generate() {
        return new WorkflowDefinitionId(UUID.randomUUID().toString());
    }

    public static WorkflowDefinitionId of(String value) {
        return new WorkflowDefinitionId(value);
    }
}
