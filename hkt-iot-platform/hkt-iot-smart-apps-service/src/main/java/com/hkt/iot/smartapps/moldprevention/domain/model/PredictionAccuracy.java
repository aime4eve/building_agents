package com.hkt.iot.smartapps.moldprevention.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预测准确率值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionAccuracy {

    /**
     * 准确率（0-1）
     */
    private BigDecimal accuracy;

    /**
     * 总预测次数
     */
    private int totalPredictions;

    /**
     * 正确预测次数
     */
    private int correctPredictions;

    /**
     * 计算时间
     */
    private LocalDateTime calculatedAt;

    /**
     * 计算方法说明
     */
    private String calculationMethod;

    /**
     * 检查准确率是否达标
     */
    public boolean isAcceptable() {
        return accuracy != null && accuracy.compareTo(BigDecimal.valueOf(0.8)) >= 0;
    }
}
