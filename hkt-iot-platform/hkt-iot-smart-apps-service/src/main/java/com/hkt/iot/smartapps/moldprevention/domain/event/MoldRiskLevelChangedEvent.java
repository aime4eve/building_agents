package com.hkt.iot.smartapps.moldprevention.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 霉菌风险等级变更领域事件
 */
@Getter
public class MoldRiskLevelChangedEvent extends DomainEvent {

    private final String zoneId;
    private final String zoneCode;
    private final Long tenantId;
    private final String previousRiskLevel;
    private final String currentRiskLevel;
    private final double temperature;
    private final double humidity;
    private final LocalDateTime changedAt;

    public MoldRiskLevelChangedEvent(
            String zoneId,
            String zoneCode,
            Long tenantId,
            String previousRiskLevel,
            String currentRiskLevel,
            double temperature,
            double humidity,
            LocalDateTime changedAt) {
        this.zoneId = zoneId;
        this.zoneCode = zoneCode;
        this.tenantId = tenantId;
        this.previousRiskLevel = previousRiskLevel;
        this.currentRiskLevel = currentRiskLevel;
        this.temperature = temperature;
        this.humidity = humidity;
        this.changedAt = changedAt;
    }

    @Override
    public String eventType() {
        return "MoldRiskLevelChanged";
    }
}
