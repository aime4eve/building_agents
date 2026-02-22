package com.hkt.iot.smartapps.moldprevention.domain.service.impl;

import com.hkt.iot.smartapps.moldprevention.domain.model.*;
import com.hkt.iot.smartapps.moldprevention.domain.repository.MoldPreventionZoneRepository;
import com.hkt.iot.smartapps.moldprevention.domain.service.MoldRiskEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 霉菌风险评估领域服务实现
 *
 * 风险评分算法：
 * - 基础分数 = 湿度分数 + 温度分数
 * - 湿度分数: <50% = 0分, 50-60% = 20分, 60-70% = 40分, 70-80% = 60分, 80-90% = 80分, >90% = 100分
 * - 温度分数: <15°C = 10分, 15-20°C = 30分, 20-30°C = 60分, 30-35°C = 80分, >35°C = 40分
 * - 综合评分 = 湿度分数 * 0.6 + 温度分数 * 0.4
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MoldRiskEvaluationServiceImpl implements MoldRiskEvaluationService {

    private final MoldPreventionZoneRepository zoneRepository;

    @Override
    public MoldRiskEvaluationResult evaluateRisk(ZoneId zoneId, EnvironmentData data) {
        log.debug("评估霉菌风险: zoneId={}, temperature={}, humidity={}", 
                zoneId.getValue(), data.getTemperature(), data.getHumidity());
        
        MoldPreventionZone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("防霉管控区域不存在: " + zoneId.getValue()));
        
        return zone.evaluateRisk(data);
    }

    @Override
    public int calculateRiskScore(EnvironmentData data) {
        double humidity = data.getHumidity();
        double temperature = data.getTemperature();
        
        int humidityScore = calculateHumidityScore(humidity);
        int temperatureScore = calculateTemperatureScore(temperature);
        
        int totalScore = (int) (humidityScore * 0.6 + temperatureScore * 0.4);
        
        log.debug("计算风险分数: humidityScore={}, temperatureScore={}, totalScore={}", 
                humidityScore, temperatureScore, totalScore);
        
        return totalScore;
    }

    @Override
    public MoldRiskLevel predictRisk(ZoneId zoneId, EnvironmentData currentData, int hoursAhead) {
        log.debug("预测未来风险: zoneId={}, hoursAhead={}", zoneId.getValue(), hoursAhead);
        
        MoldPreventionZone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("防霉管控区域不存在: " + zoneId.getValue()));
        
        double predictedHumidity = predictHumidity(currentData, hoursAhead);
        double predictedTemperature = predictTemperature(currentData, hoursAhead);
        
        EnvironmentData predictedData = EnvironmentData.builder()
                .temperature(predictedTemperature)
                .humidity(predictedHumidity)
                .timestamp(LocalDateTime.now().plusHours(hoursAhead))
                .build();
        
        int riskScore = calculateRiskScore(predictedData);
        return mapScoreToRiskLevel(riskScore);
    }

    @Override
    public List<MoldRiskStatistics> getRiskTrend(ZoneId zoneId, LocalDateTime from, LocalDateTime to) {
        log.debug("获取风险趋势: zoneId={}, from={}, to={}", zoneId.getValue(), from, to);
        
        List<MoldRiskStatistics> trends = new ArrayList<>();
        
        MoldPreventionZone zone = zoneRepository.findById(zoneId).orElse(null);
        if (zone == null) {
            return trends;
        }
        
        return trends;
    }

    @Override
    public List<MoldRiskEvaluationResult> batchEvaluateRisk(List<ZoneId> zoneIds, List<EnvironmentData> dataList) {
        log.debug("批量评估风险: count={}", zoneIds.size());
        
        List<MoldRiskEvaluationResult> results = new ArrayList<>();
        
        for (int i = 0; i < zoneIds.size() && i < dataList.size(); i++) {
            try {
                MoldRiskEvaluationResult result = evaluateRisk(zoneIds.get(i), dataList.get(i));
                results.add(result);
            } catch (Exception e) {
                log.error("评估风险失败: zoneId={}", zoneIds.get(i).getValue(), e);
            }
        }
        
        return results;
    }

    private int calculateHumidityScore(double humidity) {
        if (humidity < 50) {
            return 0;
        } else if (humidity < 60) {
            return 20;
        } else if (humidity < 70) {
            return 40;
        } else if (humidity < 80) {
            return 60;
        } else if (humidity < 90) {
            return 80;
        } else {
            return 100;
        }
    }

    private int calculateTemperatureScore(double temperature) {
        if (temperature < 15) {
            return 10;
        } else if (temperature < 20) {
            return 30;
        } else if (temperature < 30) {
            return 60;
        } else if (temperature < 35) {
            return 80;
        } else {
            return 40;
        }
    }

    private MoldRiskLevel mapScoreToRiskLevel(int score) {
        if (score < 20) {
            return MoldRiskLevel.LOW;
        } else if (score < 40) {
            return MoldRiskLevel.MEDIUM;
        } else if (score < 60) {
            return MoldRiskLevel.HIGH;
        } else {
            return MoldRiskLevel.CRITICAL;
        }
    }

    private double predictHumidity(EnvironmentData currentData, int hoursAhead) {
        double currentHumidity = currentData.getHumidity();
        
        double trend = 0;
        
        double predictedHumidity = currentHumidity + trend * hoursAhead;
        
        return Math.max(0, Math.min(100, predictedHumidity));
    }

    private double predictTemperature(EnvironmentData currentData, int hoursAhead) {
        double currentTemperature = currentData.getTemperature();
        
        double trend = 0;
        
        double predictedTemperature = currentTemperature + trend * hoursAhead;
        
        return predictedTemperature;
    }
}
