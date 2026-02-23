package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Objects;

/**
 * 活动 ID - 值对象
 *
 * @author HKT IoT Team
 */
@Value
@EqualsAndHashCode(of = "value")
public class ActivityId {
    String value;

    private ActivityId(String value) {
        this.value = Objects.requireNonNull(value, "ActivityId cannot be null");
    }

    public static ActivityId of(String value) {
        return new ActivityId(value);
    }
}
