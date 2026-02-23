package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Objects;

/**
 * 业务键 - 值对象
 *
 * @author HKT IoT Team
 */
@Value
@EqualsAndHashCode(of = "value")
public class BusinessKey {
    String value;

    private BusinessKey(String value) {
        this.value = Objects.requireNonNull(value, "BusinessKey cannot be null");
    }

    public static BusinessKey of(String value) {
        return new BusinessKey(value);
    }
}
