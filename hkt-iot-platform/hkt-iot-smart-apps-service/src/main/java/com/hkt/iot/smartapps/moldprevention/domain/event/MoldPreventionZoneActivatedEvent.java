package com.hkt.iot.smartapps.moldprevention.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 防霉管控区域激活领域事件
 */
@Getter
public class MoldPreventionZoneActivatedEvent extends DomainEvent {

    private final String zoneId;
    private final String zoneCode;
    private final Long tenantId;
    private final int sensorCount;
    private final int controllerCount;
    private final LocalDateTime activatedAt;

    public MoldPreventionZoneActivatedEvent(
            String zoneId,
            String zoneCode,
            Long tenantId,
            int sensorCount,
            int controllerCount,
            LocalDateTime activatedAt) {
        this.zoneId = zoneId;
        this.zoneCode = zoneCode;
        this.tenantId = tenantId;
        this.sensorCount = sensorCount;
        this.controllerCount = controllerCount;
        this.activatedAt = activatedAt;
    }

    @Override
    public String eventType() {
        return "MoldPreventionZoneActivated";
    }
}
