package com.hkt.iot.smartapps.moldprevention.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 防霉效果报告生成领域事件
 */
@Getter
public class MoldPreventionReportGeneratedEvent extends DomainEvent {

    private final String reportId;
    private final String zoneId;
    private final String zoneCode;
    private final Long tenantId;
    private final String period;
    private final double predictionAccuracy;
    private final String status;
    private final LocalDateTime generatedAt;

    public MoldPreventionReportGeneratedEvent(
            String reportId,
            String zoneId,
            String zoneCode,
            Long tenantId,
            String period,
            double predictionAccuracy,
            String status,
            LocalDateTime generatedAt) {
        this.reportId = reportId;
        this.zoneId = zoneId;
        this.zoneCode = zoneCode;
        this.tenantId = tenantId;
        this.period = period;
        this.predictionAccuracy = predictionAccuracy;
        this.status = status;
        this.generatedAt = generatedAt;
    }

    @Override
    public String eventType() {
        return "MoldPreventionReportGenerated";
    }
}
