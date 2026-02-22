package com.hkt.iot.smartapps.smartlivestock.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 生长效率值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthEfficiency {

    /**
     * 每单位饲料增重（kg/kg）
     */
    private BigDecimal weightGainPerFeed;

    /**
     * 饲料转化率
     */
    private BigDecimal feedConversionRatio;

    /**
     * 效率评级（1-10）
     */
    private int efficiencyRating;

    /**
     * 评估日期
     */
    private LocalDate evaluatedAt;

    /**
     * 判断效率是否优秀
     */
    public boolean isExcellent() {
        return efficiencyRating >= 8;
    }

    /**
     * 计算生长效率
     */
    public static GrowthEfficiency calculate(
            Weight initialWeight,
            Weight finalWeight,
            BigDecimal totalFeedIntake) {

        if (totalFeedIntake == null || totalFeedIntake.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("饲料摄入量必须大于0");
        }

        BigDecimal weightGain = finalWeight.toKilograms().getValue()
                .subtract(initialWeight.toKilograms().getValue());

        // 每单位饲料增重
        BigDecimal weightGainPerFeed = weightGain.divide(totalFeedIntake, 4, BigDecimal.ROUND_HALF_UP);

        // 饲料转化率（增重/饲料）
        BigDecimal fcr = totalFeedIntake.divide(weightGain, 2, BigDecimal.ROUND_HALF_UP);

        // 效率评级（基于FCR）
        int rating = calculateRating(fcr);

        return GrowthEfficiency.builder()
                .weightGainPerFeed(weightGainPerFeed)
                .feedConversionRatio(fcr)
                .efficiencyRating(rating)
                .evaluatedAt(LocalDate.now())
                .build();
    }

    private static int calculateRating(BigDecimal fcr) {
        if (fcr.compareTo(BigDecimal.valueOf(3.0)) <= 0) {
            return 10;
        } else if (fcr.compareTo(BigDecimal.valueOf(4.0)) <= 0) {
            return 8;
        } else if (fcr.compareTo(BigDecimal.valueOf(5.0)) <= 0) {
            return 6;
        } else if (fcr.compareTo(BigDecimal.valueOf(6.0)) <= 0) {
            return 4;
        } else {
            return 2;
        }
    }
}
