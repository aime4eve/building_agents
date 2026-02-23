package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Objects;

/**
 * 流程定义键 - 值对象
 *
 * @author HKT IoT Team
 */
@Value
@EqualsAndHashCode(of = "value")
public class ProcessDefinitionKey {
    String value;

    private ProcessDefinitionKey(String value) {
        this.value = Objects.requireNonNull(value, "ProcessDefinitionKey cannot be null");
    }

    public static ProcessDefinitionKey of(String value) {
        return new ProcessDefinitionKey(value);
    }
}
