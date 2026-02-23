package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Objects;
import java.util.UUID;

/**
 * SLA 监控 ID - 值对象
 *
 * @author HKT IoT Team
 */
@Value
@EqualsAndHashCode(of = "value")
public class SLAMonitorId {
    String value;

    private SLAMonitorId(String value) {
        this.value = Objects.requireNonNull(value, "SLAMonitorId cannot be null");
    }

    public static SLAMonitorId generate() {
        return new SLAMonitorId(UUID.randomUUID().toString());
    }

    public static SLAMonitorId of(String value) {
        return new SLAMonitorId(value);
    }
}
