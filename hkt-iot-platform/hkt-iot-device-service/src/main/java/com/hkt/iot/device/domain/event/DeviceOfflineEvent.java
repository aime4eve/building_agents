package com.hkt.iot.device.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 设备离线领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class DeviceOfflineEvent extends DomainEvent {

    private final Long deviceId;
    private final String deviceSn;
    private final Long tenantId;
    private final String reason;
    private final LocalDateTime offlineAt;

    public DeviceOfflineEvent(
            Long deviceId,
            String deviceSn,
            Long tenantId,
            String reason,
            LocalDateTime offlineAt) {
        this.deviceId = deviceId;
        this.deviceSn = deviceSn;
        this.tenantId = tenantId;
        this.reason = reason;
        this.offlineAt = offlineAt;
    }

    @Override
    public String eventType() {
        return "DeviceOffline";
    }
}
