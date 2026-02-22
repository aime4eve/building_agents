package com.hkt.iot.smartapps.moldprevention.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 霉菌风险统计值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoldRiskStatistics {

    /**
     * 平均风险等级
     */
    private BigDecimal averageRiskLevel;

    /**
     * 最高风险等级
     */
    private MoldRiskLevel maxRiskLevel;

    /**
     * 各风险等级时长统计（小时）
     */
    private long lowRiskHours;
    private long mediumRiskHours;
    private long highRiskHours;
    private long criticalRiskHours;

    /**
     * 风险变化次数
     */
    private int riskChangeCount;

    /**
     * 统计时间范围
     */
    private LocalDateTime statisticsFrom;
    private LocalDateTime statisticsTo;
}
