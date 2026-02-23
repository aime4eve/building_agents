package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Objects;

/**
 * 流程定义键 - 值对象
 * 用于唯一标识一个流程定义的业务键
 *
 * @author HKT IoT Team
 */
@Value
@EqualsAndHashCode(of = "value")
public class WorkflowDefinitionKey {
    String value;

    private WorkflowDefinitionKey(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("WorkflowDefinitionKey cannot be empty");
        }
        this.value = value;
    }

    public static WorkflowDefinitionKey of(String value) {
        return new WorkflowDefinitionKey(value);
    }
}
