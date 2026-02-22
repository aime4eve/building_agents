package com.hkt.iot.smartapps.smartlivestock.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 健康记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecord {

    private String id;
    private LivestockId livestockId;
    private HealthIndicator indicator;
    private BigDecimal value;
    private String unit;
    private LocalDateTime recordedAt;
    private String notes;

    /**
     * 判断是否正常
     */
    public boolean isNormal() {
        if (indicator == null || value == null) {
            return false;
        }
        return value.compareTo(indicator.getNormalMin()) >= 0
                && value.compareTo(indicator.getNormalMax()) <= 0;
    }

    /**
     * 健康指标枚举
     */
    public enum HealthIndicator {
        TEMPERATURE("体温", "°C", 38.0, 39.5),
        PH("pH值", "", 5.5, 7.5),
        ACTIVITY("活动量", "步", 1000, 10000),
        HEART_RATE("心率", "bpm", 60, 100);

        private final String name;
        private final String unit;
        private final BigDecimal normalMin;
        private final BigDecimal normalMax;

        HealthIndicator(String name, String unit, double normalMin, double normalMax) {
            this.name = name;
            this.unit = unit;
            this.normalMin = BigDecimal.valueOf(normalMin);
            this.normalMax = BigDecimal.valueOf(normalMax);
        }

        public String getName() {
            return name;
        }

        public String getUnit() {
            return unit;
        }

        public BigDecimal getNormalMin() {
            return normalMin;
        }

        public BigDecimal getNormalMax() {
            return normalMax;
        }
    }
}
