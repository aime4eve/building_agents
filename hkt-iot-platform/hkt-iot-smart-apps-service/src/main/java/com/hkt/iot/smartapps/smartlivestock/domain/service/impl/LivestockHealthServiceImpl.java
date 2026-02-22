package com.hkt.iot.smartapps.smartlivestock.domain.service.impl;

import com.hkt.iot.smartapps.smartlivestock.domain.model.HealthAlert;
import com.hkt.iot.smartapps.smartlivestock.domain.model.HealthLevel;
import com.hkt.iot.smartapps.smartlivestock.domain.model.HealthRecord;
import com.hkt.iot.smartapps.smartlivestock.domain.model.HealthScore;
import com.hkt.iot.smartapps.smartlivestock.domain.model.HealthStatistics;
import com.hkt.iot.smartapps.smartlivestock.domain.model.Livestock;
import com.hkt.iot.smartapps.smartlivestock.domain.model.LivestockHealthReport;
import com.hkt.iot.smartapps.smartlivestock.domain.model.LivestockId;
import com.hkt.iot.smartapps.smartlivestock.domain.model.ReportPeriod;
import com.hkt.iot.smartapps.smartlivestock.domain.repository.LivestockRepository;
import com.hkt.iot.smartapps.smartlivestock.domain.service.LivestockHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 牲畜健康领域服务实现类
 *
 * 职责：实现牲畜健康相关的业务逻辑
 * 健康评分算法：
 * - 体温 (权重30%): 正常范围 37.5-39.5°C
 * - 心率 (权重20%): 正常范围 60-80 bpm
 * - 呼吸频率 (权重15%): 正常范围 15-30 次/分钟
 * - 运动量 (权重20%): 正常范围 2000-8000 步/天
 * - 采食量 (权重15%): 基于历史平均值偏差
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LivestockHealthServiceImpl implements LivestockHealthService {

    private final LivestockRepository livestockRepository;

    private static final BigDecimal TEMP_NORMAL_MIN = new BigDecimal("37.5");
    private static final BigDecimal TEMP_NORMAL_MAX = new BigDecimal("39.5");
    private static final double TEMP_WEIGHT = 0.30;

    private static final BigDecimal HEART_RATE_NORMAL_MIN = new BigDecimal("60");
    private static final BigDecimal HEART_RATE_NORMAL_MAX = new BigDecimal("80");
    private static final double HEART_RATE_WEIGHT = 0.20;

    private static final BigDecimal RESPIRATORY_RATE_NORMAL_MIN = new BigDecimal("15");
    private static final BigDecimal RESPIRATORY_RATE_NORMAL_MAX = new BigDecimal("30");
    private static final double RESPIRATORY_RATE_WEIGHT = 0.15;

    private static final BigDecimal ACTIVITY_NORMAL_MIN = new BigDecimal("2000");
    private static final BigDecimal ACTIVITY_NORMAL_MAX = new BigDecimal("8000");
    private static final double ACTIVITY_WEIGHT = 0.20;

    private static final double FEED_INTAKE_WEIGHT = 0.15;

    @Override
    public HealthScore calculateHealthScore(LivestockId livestockId, HealthRecord record) {
        log.debug("计算牲畜健康评分, livestockId={}, recordId={}", livestockId, record.getId());

        double score = 0.0;

        score += calculateTemperatureScore(record) * TEMP_WEIGHT;
        score += calculateHeartRateScore(record) * HEART_RATE_WEIGHT;
        score += calculateRespiratoryRateScore(record) * RESPIRATORY_RATE_WEIGHT;
        score += calculateActivityScore(record) * ACTIVITY_WEIGHT;
        score += calculateFeedIntakeScore(livestockId, record) * FEED_INTAKE_WEIGHT;

        int finalScore = (int) Math.round(Math.max(0, Math.min(100, score)));
        log.debug("健康评分计算完成, livestockId={}, score={}", livestockId, finalScore);

        return HealthScore.of(finalScore);
    }

    @Override
    public HealthStatistics analyzeHealthTrend(LivestockId livestockId, LocalDateTime from, LocalDateTime to) {
        log.debug("分析健康趋势, livestockId={}, from={}, to={}", livestockId, from, to);

        List<HealthRecord> records = findHealthRecords(livestockId, from, to);

        if (records.isEmpty()) {
            return HealthStatistics.builder()
                    .averageHealthScore(BigDecimal.ZERO)
                    .totalRecords(0)
                    .alertCount(0)
                    .dominantLevel(HealthLevel.FAIR)
                    .statisticsFrom(from)
                    .statisticsTo(to)
                    .build();
        }

        BigDecimal avgScore = calculateAverageScore(records);
        HealthLevel dominantLevel = determineDominantLevel(records);
        int alertCount = countAlerts(records);

        return HealthStatistics.builder()
                .averageHealthScore(avgScore)
                .totalRecords(records.size())
                .alertCount(alertCount)
                .dominantLevel(dominantLevel)
                .statisticsFrom(from)
                .statisticsTo(to)
                .build();
    }

    @Override
    public boolean detectAnomaly(LivestockId livestockId, HealthRecord record) {
        log.debug("检测异常, livestockId={}, recordId={}", livestockId, record.getId());

        boolean temperatureAnomaly = isTemperatureAnomaly(record);
        boolean heartRateAnomaly = isHeartRateAnomaly(record);
        boolean respiratoryAnomaly = isRespiratoryRateAnomaly(record);
        boolean activityAnomaly = isActivityAnomaly(record);
        boolean feedIntakeAnomaly = isFeedIntakeAnomaly(livestockId, record);

        boolean hasAnomaly = temperatureAnomaly || heartRateAnomaly || respiratoryAnomaly || activityAnomaly || feedIntakeAnomaly;

        if (hasAnomaly) {
            log.warn("检测到健康异常, livestockId={}, temperatureAnomaly={}, heartRateAnomaly={}, respiratoryAnomaly={}, activityAnomaly={}, feedIntakeAnomaly={}",
                    livestockId, temperatureAnomaly, heartRateAnomaly, respiratoryAnomaly, activityAnomaly, feedIntakeAnomaly);
        }

        return hasAnomaly;
    }

    @Override
    public LivestockHealthReport generateHealthReport(LivestockId livestockId, ReportPeriod period) {
        log.debug("生成健康报告, livestockId={}, period={}", livestockId, period);

        LivestockHealthReport report = LivestockHealthReport.create(livestockId, period);

        try {
            LocalDateTime from = period.getStartDate().atStartOfDay();
            LocalDateTime to = period.getEndDate().atTime(23, 59, 59);

            List<HealthRecord> records = findHealthRecords(livestockId, from, to);
            HealthStatistics stats = analyzeHealthTrend(livestockId, from, to);
            List<HealthAlert> alerts = generateAlerts(livestockId, records);

            report.setHealthStats(stats);
            report.setHealthRecords(records);
            report.setAlerts(alerts);
            report.markAsCompleted();

            log.info("健康报告生成完成, livestockId={}, recordCount={}, alertCount={}",
                    livestockId, records.size(), alerts.size());

        } catch (Exception e) {
            log.error("健康报告生成失败, livestockId={}", livestockId, e);
            report.markAsFailed(e.getMessage());
        }

        return report;
    }

    @Override
    public List<HealthScore> batchCalculateHealthScore(List<LivestockId> livestockIds) {
        log.debug("批量计算健康评分, count={}", livestockIds.size());

        List<HealthScore> scores = new ArrayList<>();

        for (LivestockId livestockId : livestockIds) {
            try {
                Optional<Livestock> livestockOpt = livestockRepository.findById(livestockId);
                if (livestockOpt.isPresent()) {
                    HealthRecord latestRecord = findLatestHealthRecord(livestockId);
                    if (latestRecord != null) {
                        scores.add(calculateHealthScore(livestockId, latestRecord));
                    }
                }
            } catch (Exception e) {
                log.warn("批量计算健康评分失败, livestockId={}", livestockId, e);
            }
        }

        return scores;
    }

    private double calculateTemperatureScore(HealthRecord record) {
        if (record.getIndicator() == HealthRecord.HealthIndicator.TEMPERATURE && record.getValue() != null) {
            BigDecimal temp = record.getValue();
            if (temp.compareTo(TEMP_NORMAL_MIN) >= 0 && temp.compareTo(TEMP_NORMAL_MAX) <= 0) {
                return 100.0;
            }
            BigDecimal deviation;
            if (temp.compareTo(TEMP_NORMAL_MIN) < 0) {
                deviation = TEMP_NORMAL_MIN.subtract(temp);
            } else {
                deviation = temp.subtract(TEMP_NORMAL_MAX);
            }
            double penalty = deviation.doubleValue() * 20;
            return Math.max(0, 100 - penalty);
        }
        return 70.0;
    }

    private double calculateHeartRateScore(HealthRecord record) {
        if (record.getIndicator() == HealthRecord.HealthIndicator.HEART_RATE && record.getValue() != null) {
            BigDecimal heartRate = record.getValue();
            if (heartRate.compareTo(HEART_RATE_NORMAL_MIN) >= 0 && heartRate.compareTo(HEART_RATE_NORMAL_MAX) <= 0) {
                return 100.0;
            }
            BigDecimal deviation;
            if (heartRate.compareTo(HEART_RATE_NORMAL_MIN) < 0) {
                deviation = HEART_RATE_NORMAL_MIN.subtract(heartRate);
            } else {
                deviation = heartRate.subtract(HEART_RATE_NORMAL_MAX);
            }
            double penalty = deviation.doubleValue() * 2;
            return Math.max(0, 100 - penalty);
        }
        return 70.0;
    }

    private double calculateRespiratoryRateScore(HealthRecord record) {
        BigDecimal respiratoryRate = extractRespiratoryRate(record);
        if (respiratoryRate != null) {
            if (respiratoryRate.compareTo(RESPIRATORY_RATE_NORMAL_MIN) >= 0
                    && respiratoryRate.compareTo(RESPIRATORY_RATE_NORMAL_MAX) <= 0) {
                return 100.0;
            }
            BigDecimal deviation;
            if (respiratoryRate.compareTo(RESPIRATORY_RATE_NORMAL_MIN) < 0) {
                deviation = RESPIRATORY_RATE_NORMAL_MIN.subtract(respiratoryRate);
            } else {
                deviation = respiratoryRate.subtract(RESPIRATORY_RATE_NORMAL_MAX);
            }
            double penalty = deviation.doubleValue() * 5;
            return Math.max(0, 100 - penalty);
        }
        return 70.0;
    }

    private double calculateActivityScore(HealthRecord record) {
        if (record.getIndicator() == HealthRecord.HealthIndicator.ACTIVITY && record.getValue() != null) {
            BigDecimal activity = record.getValue();
            if (activity.compareTo(ACTIVITY_NORMAL_MIN) >= 0 && activity.compareTo(ACTIVITY_NORMAL_MAX) <= 0) {
                return 100.0;
            }
            BigDecimal deviation;
            if (activity.compareTo(ACTIVITY_NORMAL_MIN) < 0) {
                deviation = ACTIVITY_NORMAL_MIN.subtract(activity);
            } else {
                deviation = activity.subtract(ACTIVITY_NORMAL_MAX);
            }
            double penalty = deviation.doubleValue() / 100;
            return Math.max(0, 100 - penalty);
        }
        return 70.0;
    }

    private double calculateFeedIntakeScore(LivestockId livestockId, HealthRecord record) {
        BigDecimal feedIntake = extractFeedIntake(record);
        if (feedIntake != null) {
            BigDecimal historicalAvg = getHistoricalFeedIntakeAverage(livestockId);
            if (historicalAvg != null && historicalAvg.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal deviation = feedIntake.subtract(historicalAvg).abs();
                double deviationPercent = deviation.divide(historicalAvg, 4, RoundingMode.HALF_UP).doubleValue();
                if (deviationPercent <= 0.1) {
                    return 100.0;
                } else if (deviationPercent <= 0.2) {
                    return 80.0;
                } else if (deviationPercent <= 0.3) {
                    return 60.0;
                } else {
                    return 40.0;
                }
            }
        }
        return 70.0;
    }

    private boolean isTemperatureAnomaly(HealthRecord record) {
        if (record.getIndicator() == HealthRecord.HealthIndicator.TEMPERATURE && record.getValue() != null) {
            BigDecimal temp = record.getValue();
            return temp.compareTo(TEMP_NORMAL_MIN) < 0 || temp.compareTo(TEMP_NORMAL_MAX) > 0;
        }
        return false;
    }

    private boolean isHeartRateAnomaly(HealthRecord record) {
        if (record.getIndicator() == HealthRecord.HealthIndicator.HEART_RATE && record.getValue() != null) {
            BigDecimal heartRate = record.getValue();
            return heartRate.compareTo(HEART_RATE_NORMAL_MIN) < 0 || heartRate.compareTo(HEART_RATE_NORMAL_MAX) > 0;
        }
        return false;
    }

    private boolean isRespiratoryRateAnomaly(HealthRecord record) {
        BigDecimal respiratoryRate = extractRespiratoryRate(record);
        if (respiratoryRate != null) {
            return respiratoryRate.compareTo(RESPIRATORY_RATE_NORMAL_MIN) < 0
                    || respiratoryRate.compareTo(RESPIRATORY_RATE_NORMAL_MAX) > 0;
        }
        return false;
    }

    private boolean isActivityAnomaly(HealthRecord record) {
        if (record.getIndicator() == HealthRecord.HealthIndicator.ACTIVITY && record.getValue() != null) {
            BigDecimal activity = record.getValue();
            return activity.compareTo(ACTIVITY_NORMAL_MIN) < 0 || activity.compareTo(ACTIVITY_NORMAL_MAX) > 0;
        }
        return false;
    }

    private boolean isFeedIntakeAnomaly(LivestockId livestockId, HealthRecord record) {
        BigDecimal feedIntake = extractFeedIntake(record);
        if (feedIntake != null) {
            BigDecimal historicalAvg = getHistoricalFeedIntakeAverage(livestockId);
            if (historicalAvg != null && historicalAvg.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal deviation = feedIntake.subtract(historicalAvg).abs();
                double deviationPercent = deviation.divide(historicalAvg, 4, RoundingMode.HALF_UP).doubleValue();
                return deviationPercent > 0.3;
            }
        }
        return false;
    }

    private BigDecimal extractRespiratoryRate(HealthRecord record) {
        return null;
    }

    private BigDecimal extractFeedIntake(HealthRecord record) {
        return null;
    }

    private BigDecimal getHistoricalFeedIntakeAverage(LivestockId livestockId) {
        return new BigDecimal("10.0");
    }

    private List<HealthRecord> findHealthRecords(LivestockId livestockId, LocalDateTime from, LocalDateTime to) {
        return new ArrayList<>();
    }

    private HealthRecord findLatestHealthRecord(LivestockId livestockId) {
        return null;
    }

    private BigDecimal calculateAverageScore(List<HealthRecord> records) {
        return BigDecimal.valueOf(75.0);
    }

    private HealthLevel determineDominantLevel(List<HealthRecord> records) {
        return HealthLevel.GOOD;
    }

    private int countAlerts(List<HealthRecord> records) {
        return 0;
    }

    private List<HealthAlert> generateAlerts(LivestockId livestockId, List<HealthRecord> records) {
        List<HealthAlert> alerts = new ArrayList<>();

        for (HealthRecord record : records) {
            if (isTemperatureAnomaly(record)) {
                alerts.add(HealthAlert.create(
                        livestockId,
                        HealthAlert.AlertType.TEMPERATURE_ABNORMAL,
                        HealthAlert.AlertSeverity.WARNING,
                        "体温异常: " + record.getValue() + record.getUnit()
                ));
            }
        }

        return alerts;
    }
}
