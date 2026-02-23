package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Objects;

/**
 * 用户 ID - 值对象
 *
 * @author HKT IoT Team
 */
@Value
@EqualsAndHashCode(of = "value")
public class UserId {
    String value;

    private UserId(String value) {
        this.value = Objects.requireNonNull(value, "UserId cannot be null");
    }

    public static UserId of(String value) {
        return new UserId(value);
    }
}
