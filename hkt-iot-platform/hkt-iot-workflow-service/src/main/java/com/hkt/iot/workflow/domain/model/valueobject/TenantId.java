package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Objects;

/**
 * 租户 ID - 值对象
 *
 * @author HKT IoT Team
 */
@Value
@EqualsAndHashCode(of = "value")
public class TenantId {
    String value;

    private TenantId(String value) {
        this.value = Objects.requireNonNull(value, "TenantId cannot be null");
    }

    public static TenantId of(String value) {
        return new TenantId(value);
    }
}
