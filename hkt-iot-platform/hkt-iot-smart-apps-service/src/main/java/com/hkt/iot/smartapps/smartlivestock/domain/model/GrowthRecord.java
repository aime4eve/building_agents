package com.hkt.iot.smartapps.smartlivestock.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生长记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthRecord {

    private String id;
    private LivestockId livestockId;
    private Weight weight;
    private FeedIntake feedIntake;
    private LocalDateTime recordedAt;
    private String notes;

    /**
     * 计算日均增重
     */
    public BigDecimal calculateDailyWeightGain(GrowthRecord previous) {
        if (previous == null) {
            return BigDecimal.ZERO;
        }

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                previous.getRecordedAt(),
                this.getRecordedAt()
        );

        if (daysBetween <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal weightGain = this.weight.toKilograms().getValue()
                .subtract(previous.weight.toKilograms().getValue());

        return weightGain.divide(BigDecimal.valueOf(daysBetween), 2, BigDecimal.ROUND_HALF_UP);
    }
}
