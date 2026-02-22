package com.hkt.iot.smartapps.smartlivestock.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 牲畜健康报告聚合根
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LivestockHealthReport {

    private ReportId id;
    private LivestockId livestockId;
    private ReportPeriod period;

    /**
     * 健康统计数据
     */
    private HealthStatistics healthStats;

    /**
     * 健康记录列表
     */
    private List<HealthRecord> healthRecords;

    /**
     * 告警列表
     */
    private List<HealthAlert> alerts;

    /**
     * 数据来源可追溯
     */
    private List<SourceDataTrace> dataTraces;

    private LocalDateTime generatedAt;
    private ReportStatus status;
    private String exportFormat;
    private String exportUrl;

    /**
     * 报告状态枚举
     */
    public enum ReportStatus {
        GENERATING,
        COMPLETED,
        FAILED
    }

    /**
     * 创建新报告
     */
    public static LivestockHealthReport create(
            LivestockId livestockId,
            ReportPeriod period) {

        return LivestockHealthReport.builder()
                .id(ReportId.generate())
                .livestockId(livestockId)
                .period(period)
                .status(ReportStatus.GENERATING)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 标记为已完成
     */
    public void markAsCompleted() {
        this.status = ReportStatus.COMPLETED;
        this.generatedAt = LocalDateTime.now();
    }

    /**
     * 标记为失败
     */
    public void markAsFailed(String reason) {
        this.status = ReportStatus.FAILED;
        this.generatedAt = LocalDateTime.now();
    }
}
