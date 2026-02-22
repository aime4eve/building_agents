package com.hkt.iot.smartapps.moldprevention.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 防霉管控区域创建领域事件
 */
@Getter
public class MoldPreventionZoneCreatedEvent extends DomainEvent {

    private final String zoneId;
    private final String zoneCode;
    private final String zoneName;
    private final Long tenantId;
    private final Long spaceId;
    private final LocalDateTime createdAt;

    public MoldPreventionZoneCreatedEvent(
            String zoneId,
            String zoneCode,
            String zoneName,
            Long tenantId,
            Long spaceId,
            LocalDateTime createdAt) {
        this.zoneId = zoneId;
        this.zoneCode = zoneCode;
        this.zoneName = zoneName;
        this.tenantId = tenantId;
        this.spaceId = spaceId;
        this.createdAt = createdAt;
    }

    @Override
    public String eventType() {
        return "MoldPreventionZoneCreated";
    }
}
