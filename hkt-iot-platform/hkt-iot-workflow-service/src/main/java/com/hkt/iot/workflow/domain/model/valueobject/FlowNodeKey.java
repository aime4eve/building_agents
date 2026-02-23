package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Objects;

/**
 * 流程节点键 - 值对象
 *
 * @author HKT IoT Team
 */
@Value
@EqualsAndHashCode(of = "value")
public class FlowNodeKey {
    String value;

    private FlowNodeKey(String value) {
        this.value = Objects.requireNonNull(value, "FlowNodeKey cannot be null");
    }

    public static FlowNodeKey of(String value) {
        return new FlowNodeKey(value);
    }
}
