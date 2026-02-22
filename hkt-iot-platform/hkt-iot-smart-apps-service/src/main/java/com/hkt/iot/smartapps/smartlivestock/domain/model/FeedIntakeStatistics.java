package com.hkt.iot.smartapps.smartlivestock.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 饲料摄入统计值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedIntakeStatistics {

    /**
     * 总饲料摄入量
     */
    private BigDecimal totalFeedIntake;

    /**
     * 日均摄入量
     */
    private BigDecimal averageDailyIntake;

    /**
     * 单位
     */
    private String unit;
}
