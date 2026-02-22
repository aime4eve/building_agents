package com.hkt.iot.smartapps.smartlivestock.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 健康统计数据值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatistics {

    /**
     * 平均健康评分
     */
    private BigDecimal averageHealthScore;

    /**
     * 总记录数
     */
    private int totalRecords;

    /**
     * 告警数量
     */
    private int alertCount;

    /**
     * 主要健康等级
     */
    private HealthLevel dominantLevel;

    /**
     * 统计时间范围
     */
    private LocalDateTime statisticsFrom;
    private LocalDateTime statisticsTo;
}
