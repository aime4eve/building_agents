package com.hkt.iot.smartapps.smartlivestock.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生长报告聚合根
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthReport {

    private ReportId id;
    private LivestockId livestockId;
    private ReportPeriod period;

    /**
     * 生长统计数据
     */
    private WeightStatistics weightStats;

    /**
     * 饲料摄入统计
     */
    private FeedIntakeStatistics feedIntakeStats;

    /**
     * 生长记录列表
     */
    private List<GrowthRecord> growthRecords;

    /**
     * 生长效率分析
     */
    private GrowthEfficiency efficiency;

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
    public static GrowthReport create(
            LivestockId livestockId,
            ReportPeriod period) {

        return GrowthReport.builder()
                .id(ReportId.generate())
                .livestockId(livestockId)
                .period(period)
                .status(ReportStatus.GENERATING)
                .generatedAt(LocalDateTime.now())
                .build();
    }
}
