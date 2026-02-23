package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Objects;
import java.util.UUID;

/**
 * 流程实例 ID - 值对象
 *
 * @author HKT IoT Team
 */
@Value
@EqualsAndHashCode(of = "value")
public class ProcessInstanceId {
    String value;

    private ProcessInstanceId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("ProcessInstanceId cannot be empty");
        }
        this.value = value;
    }

    public static ProcessInstanceId generate() {
        return new ProcessInstanceId(UUID.randomUUID().toString());
    }

    public static ProcessInstanceId of(String value) {
        return new ProcessInstanceId(value);
    }
}
