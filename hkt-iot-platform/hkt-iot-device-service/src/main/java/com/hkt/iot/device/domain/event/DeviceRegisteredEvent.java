package com.hkt.iot.device.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 设备注册领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class DeviceRegisteredEvent extends DomainEvent {

    private final Long deviceId;
    private final String deviceSn;
    private final String deviceName;
    private final Long tenantId;
    private final String deviceType;
    private final LocalDateTime registeredAt;

    public DeviceRegisteredEvent(
            Long deviceId,
            String deviceSn,
            String deviceName,
            Long tenantId,
            String deviceType,
            LocalDateTime registeredAt) {
        this.deviceId = deviceId;
        this.deviceSn = deviceSn;
        this.deviceName = deviceName;
        this.tenantId = tenantId;
        this.deviceType = deviceType;
        this.registeredAt = registeredAt;
    }

    @Override
    public String eventType() {
        return "DeviceRegistered";
    }
}
