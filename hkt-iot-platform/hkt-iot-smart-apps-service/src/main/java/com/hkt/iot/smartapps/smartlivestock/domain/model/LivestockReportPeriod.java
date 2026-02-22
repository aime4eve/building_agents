package com.hkt.iot.smartapps.smartlivestock.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 报告周期值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportPeriod {

    private LocalDate startDate;
    private LocalDate endDate;
    private PeriodType type;

    /**
     * 周期类型枚举
     */
    public enum PeriodType {
        DAILY,
        WEEKLY,
        MONTHLY,
        CUSTOM
    }

    public static ReportPeriod daily(LocalDate date) {
        return ReportPeriod.builder()
                .startDate(date)
                .endDate(date)
                .type(PeriodType.DAILY)
                .build();
    }

    public static ReportPeriod weekly(LocalDate weekStart) {
        return ReportPeriod.builder()
                .startDate(weekStart)
                .endDate(weekStart.plusDays(6))
                .type(PeriodType.WEEKLY)
                .build();
    }

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
