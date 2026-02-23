package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Objects;
import java.util.UUID;

/**
 * 工单 ID - 值对象
 *
 * @author HKT IoT Team
 */
@Value
@EqualsAndHashCode(of = "value")
public class WorkOrderId {
    String value;

    private WorkOrderId(String value) {
        this.value = Objects.requireNonNull(value, "WorkOrderId cannot be null");
    }

    public static WorkOrderId of(String value) {
        return new WorkOrderId(value);
    }

    public static WorkOrderId generate() {
        return new WorkOrderId(UUID.randomUUID().toString().replace("-", ""));
    }
}
