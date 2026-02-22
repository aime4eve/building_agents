package com.hkt.iot.notification.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 通知统计数据传输对象
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知统计数据")
public class NotificationStatisticsDTO {

    private TimeRange timeRange;

    private Long totalCount;

    private Long successCount;

    private Long failedCount;

    private Long pendingCount;

    private Double successRate;

    private List<ChannelStatistics> channelStatistics;

    private List<TemplateStatistics> templateStatistics;

    private List<DailyStatistics> dailyStatistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "时间范围")
    public static class TimeRange {
        @Schema(description = "开始时间（Unix时间戳）")
        private Long startTime;

        @Schema(description = "结束时间（Unix时间戳）")
        private Long endTime;

        @Schema(description = "时间范围类型: DAILY/WEEKLY/MONTHLY/CUSTOM")
        private String rangeType;

        public static TimeRange daily(Instant date) {
            Instant start = date.truncatedTo(java.time.temporal.ChronoUnit.DAYS);
            Instant end = start.plus(java.time.Duration.ofDays(1));
            return TimeRange.builder()
                    .startTime(start.getEpochSecond())
                    .endTime(end.getEpochSecond())
                    .rangeType("DAILY")
                    .build();
        }

        public static TimeRange weekly(Instant date) {
            java.time.ZonedDateTime zdt = date.atZone(java.time.ZoneOffset.UTC);
            java.time.ZonedDateTime startOfWeek = zdt.with(java.time.DayOfWeek.MONDAY).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
            java.time.ZonedDateTime endOfWeek = startOfWeek.plusWeeks(1);
            return TimeRange.builder()
                    .startTime(startOfWeek.toInstant().getEpochSecond())
                    .endTime(endOfWeek.toInstant().getEpochSecond())
                    .rangeType("WEEKLY")
                    .build();
        }

        public static TimeRange monthly(Instant date) {
            java.time.ZonedDateTime zdt = date.atZone(java.time.ZoneOffset.UTC);
            java.time.ZonedDateTime startOfMonth = zdt.withDayOfMonth(1).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
            java.time.ZonedDateTime endOfMonth = startOfMonth.plusMonths(1);
            return TimeRange.builder()
                    .startTime(startOfMonth.toInstant().getEpochSecond())
                    .endTime(endOfMonth.toInstant().getEpochSecond())
                    .rangeType("MONTHLY")
                    .build();
        }

        public static TimeRange custom(Instant start, Instant end) {
            return TimeRange.builder()
                    .startTime(start.getEpochSecond())
                    .endTime(end.getEpochSecond())
                    .rangeType("CUSTOM")
                    .build();
        }

        public Instant getStartInstant() {
            return startTime != null ? Instant.ofEpochSecond(startTime) : null;
        }

        public Instant getEndInstant() {
            return endTime != null ? Instant.ofEpochSecond(endTime) : null;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "渠道统计")
    public static class ChannelStatistics {
        @Schema(description = "渠道类型")
        private String channelType;

        @Schema(description = "渠道名称")
        private String channelName;

        @Schema(description = "总发送数")
        private Long totalCount;

        @Schema(description = "成功数")
        private Long successCount;

        @Schema(description = "失败数")
        private Long failedCount;

        @Schema(description = "待发送数")
        private Long pendingCount;

        @Schema(description = "成功率")
        private Double successRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "模板统计")
    public static class TemplateStatistics {
        @Schema(description = "模板编码")
        private String templateCode;

        @Schema(description = "模板名称")
        private String templateName;

        @Schema(description = "总发送数")
        private Long totalCount;

        @Schema(description = "成功数")
        private Long successCount;

        @Schema(description = "失败数")
        private Long failedCount;

        @Schema(description = "待发送数")
        private Long pendingCount;

        @Schema(description = "成功率")
        private Double successRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "每日统计")
    public static class DailyStatistics {
        @Schema(description = "日期（Unix时间戳）")
        private Long date;

        @Schema(description = "总发送数")
        private Long totalCount;

        @Schema(description = "成功数")
        private Long successCount;

        @Schema(description = "失败数")
        private Long failedCount;

        @Schema(description = "待发送数")
        private Long pendingCount;

        @Schema(description = "成功率")
        private Double successRate;
    }

    public void calculateSuccessRate() {
        if (totalCount != null && totalCount > 0) {
            this.successRate = (successCount != null ? successCount : 0L) * 100.0 / totalCount;
        } else {
            this.successRate = 0.0;
        }
    }

    public void calculateChannelSuccessRate() {
        if (channelStatistics != null) {
            for (ChannelStatistics cs : channelStatistics) {
                if (cs.getTotalCount() != null && cs.getTotalCount() > 0) {
                    cs.setSuccessRate((cs.getSuccessCount() != null ? cs.getSuccessCount() : 0L) * 100.0 / cs.getTotalCount());
                } else {
                    cs.setSuccessRate(0.0);
                }
            }
        }
    }

    public void calculateTemplateSuccessRate() {
        if (templateStatistics != null) {
            for (TemplateStatistics ts : templateStatistics) {
                if (ts.getTotalCount() != null && ts.getTotalCount() > 0) {
                    ts.setSuccessRate((ts.getSuccessCount() != null ? ts.getSuccessCount() : 0L) * 100.0 / ts.getTotalCount());
                } else {
                    ts.setSuccessRate(0.0);
                }
            }
        }
    }

    public void calculateDailySuccessRate() {
        if (dailyStatistics != null) {
            for (DailyStatistics ds : dailyStatistics) {
                if (ds.getTotalCount() != null && ds.getTotalCount() > 0) {
                    ds.setSuccessRate((ds.getSuccessCount() != null ? ds.getSuccessCount() : 0L) * 100.0 / ds.getTotalCount());
                } else {
                    ds.setSuccessRate(0.0);
                }
            }
        }
    }
}
