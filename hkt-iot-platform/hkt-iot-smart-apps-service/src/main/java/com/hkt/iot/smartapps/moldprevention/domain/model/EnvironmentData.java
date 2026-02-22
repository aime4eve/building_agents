package com.hkt.iot.smartapps.moldprevention.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 环境数据值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentData {

    /**
     * 温度（°C）
     */
    private double temperature;

    /**
     * 湿度（%）
     */
    private double humidity;

    /**
     * 数据采集时间
     */
    private LocalDateTime collectedAt;

    /**
     * 数据来源传感器ID
     */
    private SensorId sensorId;

    /**
     * 额外数据（如气压、CO2浓度等）
     */
    private Map<String, Object> additionalData;

    /**
     * 判断数据是否有效
     */
    public boolean isValid() {
        return temperature >= -50 && temperature <= 100
                && humidity >= 0 && humidity <= 100;
    }

    /**
     * 获取体感温度（考虑温湿度）
     */
    public double getFeelsLike() {
        // 简化的体感温度计算
        if (humidity >= 60 && temperature >= 20) {
            return temperature + (humidity - 60) * 0.05;
        }
        return temperature;
    }
}
