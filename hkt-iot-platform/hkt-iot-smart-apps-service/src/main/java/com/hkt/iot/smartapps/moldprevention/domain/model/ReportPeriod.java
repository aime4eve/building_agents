package com.hkt.iot.smartapps.moldprevention.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 报告周期值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportPeriod {

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 周期类型
     */
    private PeriodType type;

    /**
     * 周期类型枚举
     */
    public enum PeriodType {
        /**
         * 日报告
         */
        DAILY,

        /**
         * 周报告
         */
        WEEKLY,

        /**
         * 月报告
         */
        MONTHLY,

        /**
         * 自定义周期
         */
        CUSTOM
    }

    /**
     * 创建日报周期
     */
    public static ReportPeriod daily(LocalDate date) {
        return ReportPeriod.builder()
                .startDate(date)
                .endDate(date)
                .type(PeriodType.DAILY)
                .build();
    }

    /**
     * 创建周报周期
     */
    public static ReportPeriod weekly(LocalDate weekStart) {
        return ReportPeriod.builder()
                .startDate(weekStart)
                .endDate(weekStart.plusDays(6))
                .type(PeriodType.WEEKLY)
                .build();
    }

    /**
     * 创建月报周期
     */
    public static ReportPeriod monthly(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return ReportPeriod.builder()
                .startDate(start)
                .endDate(end)
                .type(PeriodType.MONTHLY)
                .build();
    }
}
