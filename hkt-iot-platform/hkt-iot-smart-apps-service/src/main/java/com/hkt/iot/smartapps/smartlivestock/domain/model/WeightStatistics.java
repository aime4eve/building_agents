package com.hkt.iot.smartapps.smartlivestock.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 体重统计值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightStatistics {

    /**
     * 初始体重
     */
    private BigDecimal initialWeight;

    /**
     * 最终体重
     */
    private BigDecimal finalWeight;

    /**
     * 平均体重
     */
    private BigDecimal averageWeight;

    /**
     * 总增重
     */
    private BigDecimal weightGain;

    /**
     * 日均增重
     */
    private BigDecimal dailyGain;
}
