package com.hkt.iot.smartapps.moldprevention.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 霉菌风险阈值值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoldRiskThreshold {

    /**
     * 温度下限（°C）
     */
    private double temperatureMin;

    /**
     * 温度上限（°C）
     */
    private double temperatureMax;

    /**
     * 湿度下限（%）
     */
    private double humidityMin;

    /**
     * 湿度上限（%）
     */
    private double humidityMax;

    /**
     * 中风险湿度阈值（%）
     */
    private double mediumRiskHumidity;

    /**
     * 高风险湿度阈值（%）
     */
    private double highRiskHumidity;

    /**
     * 极高风险湿度阈值（%）
     */
    private double criticalRiskHumidity;

    /**
     * 创建默认阈值
     */
    public static MoldRiskThreshold defaultThreshold() {
        return MoldRiskThreshold.builder()
                .temperatureMin(15.0)
                .temperatureMax(35.0)
                .humidityMin(30.0)
                .humidityMax(90.0)
                .mediumRiskHumidity(65.0)
                .highRiskHumidity(75.0)
                .criticalRiskHumidity(85.0)
                .build();
    }
}
