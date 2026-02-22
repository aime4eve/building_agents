package com.hkt.iot.smartapps.moldprevention.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 控制效果评估值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlEffectiveness {

    /**
     * 控制成功率（%）
     */
    private BigDecimal successRate;

    /**
     * 响应时间（毫秒）
     */
    private long averageResponseTime;

    /**
     * 风险降低次数
     */
    private int riskReductionCount;

    /**
     * 风险升高次数
     */
    private int riskIncreaseCount;

    /**
     * 净效果（正数表示改善）
     */
    private int netEffect;

    /**
     * 评估时间
     */
    private LocalDateTime evaluatedAt;

    /**
     * 判断控制是否有效
     */
    public boolean isEffective() {
        return successRate != null && successRate.compareTo(BigDecimal.valueOf(80)) >= 0;
    }
}
