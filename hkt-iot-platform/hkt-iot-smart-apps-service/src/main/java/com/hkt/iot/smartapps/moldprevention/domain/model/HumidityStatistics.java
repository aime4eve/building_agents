package com.hkt.iot.smartapps.moldprevention.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 湿度统计值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HumidityStatistics {

    /**
     * 平均湿度（%）
     */
    private BigDecimal averageHumidity;

    /**
     * 最高湿度（%）
     */
    private BigDecimal maxHumidity;

    /**
     * 最低湿度（%）
     */
    private BigDecimal minHumidity;

    /**
     * 湿度标准差
     */
    private BigDecimal humidityStdDev;

    /**
     * 超阈值时长（小时）
     */
    private long aboveThresholdHours;

    /**
     * 统计时间范围
     */
    private LocalDateTime statisticsFrom;
    private LocalDateTime statisticsTo;
}
