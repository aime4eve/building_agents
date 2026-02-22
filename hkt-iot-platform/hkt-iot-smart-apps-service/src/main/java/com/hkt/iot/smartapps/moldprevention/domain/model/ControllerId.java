package com.hkt.iot.smartapps.moldprevention.domain.model;

import com.hkt.iot.domain.shared.DeviceId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 控制器ID值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControllerId {
    private final String value;

    public static ControllerId of(String value) {
        return new ControllerId(value);
    }

    public static ControllerId generate() {
        return new ControllerId("CTRL-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
    }

    public static ControllerId fromDeviceId(DeviceId deviceId) {
        return new ControllerId(deviceId.getValue());
    }
}
