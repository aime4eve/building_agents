package com.hkt.iot.smartapps.moldprevention.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 霉菌事件值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoldIncident {

    private String id;
    private MoldRiskLevel riskLevel;
    private LocalDateTime occurredAt;
    private LocalDateTime resolvedAt;
    private BigDecimal duration;  // 持续时间（小时）
    private String description;
    private IncidentSeverity severity;

    /**
     * 事件严重程度枚举
     */
    public enum IncidentSeverity {
        /**
         * 轻微
         */
        MINOR,

        /**
         * 中等
         */
        MODERATE,

        /**
         * 严重
         */
        SEVERE,

        /**
         * 紧急
         */
        CRITICAL
    }
}
