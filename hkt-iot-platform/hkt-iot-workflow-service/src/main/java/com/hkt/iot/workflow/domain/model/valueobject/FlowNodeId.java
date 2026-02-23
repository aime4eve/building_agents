package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.UUID;

/**
 * 流程节点 ID - 值对象
 *
 * @author HKT IoT Team
 */
@Value
@EqualsAndHashCode(of = "value")
public class FlowNodeId {
    String value;

    private FlowNodeId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("FlowNodeId cannot be empty");
        }
        this.value = value;
    }

    public static FlowNodeId generate() {
        return new FlowNodeId(UUID.randomUUID().toString());
    }

    public static FlowNodeId of(String value) {
        return new FlowNodeId(value);
    }
}
