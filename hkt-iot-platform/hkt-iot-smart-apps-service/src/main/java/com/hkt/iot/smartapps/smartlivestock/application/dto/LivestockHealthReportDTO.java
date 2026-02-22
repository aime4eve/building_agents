package com.hkt.iot.smartapps.smartlivestock.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 牲畜健康报告DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LivestockHealthReportDTO {

    /**
     * 报告ID
     */
    private String reportId;

    /**
     * 牲畜ID
     */
    private String livestockId;

    /**
     * 牲畜耳标
     */
    private String livestockTag;

    /**
     * 报告周期
     */
    private String period;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 平均健康评分
     */
    private Integer averageHealthScore;

    /**
     * 健康趋势
     */
    private String healthTrend;

    /**
     * 体重变化
     */
    private Double weightChange;

    /**
     * 异常次数
     */
    private Integer abnormalCount;

    /**
     * 健康建议
     */
    private List<String> recommendations;

    /**
     * 生成时间
     */
    private LocalDateTime generatedAt;
}
