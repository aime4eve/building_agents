package com.hkt.iot.device.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 设备上线领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class DeviceOnlineEvent extends DomainEvent {

    private final Long deviceId;
    private final String deviceSn;
    private final Long tenantId;
    private final String ipAddress;
    private final LocalDateTime onlineAt;

    public DeviceOnlineEvent(
            Long deviceId,
            String deviceSn,
            Long tenantId,
            String ipAddress,
            LocalDateTime onlineAt) {
        this.deviceId = deviceId;
        this.deviceSn = deviceSn;
        this.tenantId = tenantId;
        this.ipAddress = ipAddress;
        this.onlineAt = onlineAt;
    }

    @Override
    public String eventType() {
        return "DeviceOnline";
    }
}
