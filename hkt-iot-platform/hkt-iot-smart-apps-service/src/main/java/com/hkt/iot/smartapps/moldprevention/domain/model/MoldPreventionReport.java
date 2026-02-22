package com.hkt.iot.smartapps.moldprevention.domain.model;

import com.hkt.iot.domain.shared.AuditLog;
import com.hkt.iot.domain.shared.TenantId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 防霉效果报告聚合根
 *
 * 职责：记录防霉管控的效果分析和统计数据
 * 业务规则：
 * - 报告是历史记录，不可修改
 * - 支持周期性生成（日、周、月）
 * - 包含预测准确率评估
 */
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MoldPreventionReport {

    private ReportId id;
    private ZoneId zoneId;
    private TenantId tenantId;
    private ReportPeriod period;

    // 数据来源追踪
    private List<SensorDataTrace> sensorDataTraces;
    private LocalDateTime collectedFrom;
    private LocalDateTime collectedTo;

    // 预测结果对比
    private PredictionAccuracy predictionAccuracy;

    // 效果评估
    private MoldRiskStatistics riskStats;
    private HumidityStatistics humidityStats;
    private ControlEffectiveness effectiveness;
    private List<MoldIncident> incidents;

    // 报告生成信息
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Duration generationTime;
    private ReportStatus status;
    private String errorMessage;

    // 导出信息
    private String exportFormat;
    private String exportUrl;

    /**
     * 检查报告生成是否超时
     */
    public boolean isGenerationTimeout() {
        return this.generationTime != null && this.generationTime.toMinutes() >= 5;
    }

    /**
     * 标记报告为生成中
     */
    public void markAsGenerating() {
        this.status = ReportStatus.GENERATING;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * 标记报告为已完成
     */
    public void markAsCompleted() {
        this.status = ReportStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.generationTime = Duration.between(this.startedAt, this.completedAt);
        }
    }

    /**
     * 标记报告为失败
     */
    public void markAsFailed(String errorMessage) {
        this.status = ReportStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.generationTime = Duration.between(this.startedAt, this.completedAt);
        }
        this.errorMessage = errorMessage;
    }

    /**
     * 添加传感器数据追踪
     */
    public void addSensorDataTrace(SensorDataTrace trace) {
        if (this.sensorDataTraces == null) {
            this.sensorDataTraces = new ArrayList<>();
        }
        this.sensorDataTraces.add(trace);
    }

    /**
     * 添加霉菌事件
     */
    public void addIncident(MoldIncident incident) {
        if (this.incidents == null) {
            this.incidents = new ArrayList<>();
        }
        this.incidents.add(incident);
    }

    /**
     * 设置预测准确率
     */
    public void setPredictionAccuracy(double accuracy, int totalPredictions, int correctPredictions) {
        this.predictionAccuracy = PredictionAccuracy.builder()
                .accuracy(accuracy)
                .totalPredictions(totalPredictions)
                .correctPredictions(correctPredictions)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 检查预测准确率是否达标
     */
    public boolean isPredictionAccuracyAcceptable() {
        return this.predictionAccuracy != null
                && this.predictionAccuracy.getAccuracy() >= 0.8;  // 80%准确率要求
    }

    /**
     * 创建新报告
     */
    public static MoldPreventionReport create(
            ZoneId zoneId,
            TenantId tenantId,
            ReportPeriod period,
            LocalDateTime collectedFrom,
            LocalDateTime collectedTo) {

        return MoldPreventionReport.builder()
                .id(ReportId.generate())
                .zoneId(zoneId)
                .tenantId(tenantId)
                .period(period)
                .collectedFrom(collectedFrom)
                .collectedTo(collectedTo)
                .sensorDataTraces(new ArrayList<>())
                .incidents(new ArrayList<>())
                .status(ReportStatus.GENERATING)
                .startedAt(LocalDateTime.now())
                .build();
    }
}
