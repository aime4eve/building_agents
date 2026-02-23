package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.Getter;

import java.time.Duration;
import java.util.Objects;

/**
 * SLA 统计值对象
 * 用于汇总 SLA 监控的统计数据
 *
 * @author HKT IoT Team
 */
@Getter
public class SLAStatistics {

    private final long totalTasks;
    private final long completedTasks;
    private final long breachedTasks;
    private final Duration averageResponseTime;
    private final Duration averageResolutionTime;
    private final double slaComplianceRate;

    public SLAStatistics(
            long totalTasks,
            long completedTasks,
            long breachedTasks,
            Duration averageResponseTime,
            Duration averageResolutionTime,
            double slaComplianceRate) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.breachedTasks = breachedTasks;
        this.averageResponseTime = Objects.requireNonNull(averageResponseTime);
        this.averageResolutionTime = Objects.requireNonNull(averageResolutionTime);
        this.slaComplianceRate = slaComplianceRate;
    }

    public static SLAStatistics empty() {
        return new SLAStatistics(0, 0, 0, Duration.ZERO, Duration.ZERO, 100.0);
    }

    public static SLAStatisticsBuilder builder() {
        return new SLAStatisticsBuilder();
    }

    public long getPendingTasks() {
        return totalTasks - completedTasks;
    }

    public long getCompliantTasks() {
        return completedTasks - breachedTasks;
    }

    public double getBreachRate() {
        return totalTasks > 0 ? (double) breachedTasks / totalTasks * 100 : 0.0;
    }

    public boolean isHealthy() {
        return slaComplianceRate >= 95.0;
    }

    public boolean needsAttention() {
        return slaComplianceRate < 80.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SLAStatistics that = (SLAStatistics) o;
        return totalTasks == that.totalTasks &&
                completedTasks == that.completedTasks &&
                breachedTasks == that.breachedTasks &&
                Double.compare(that.slaComplianceRate, slaComplianceRate) == 0 &&
                Objects.equals(averageResponseTime, that.averageResponseTime) &&
                Objects.equals(averageResolutionTime, that.averageResolutionTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalTasks, completedTasks, breachedTasks,
                averageResponseTime, averageResolutionTime, slaComplianceRate);
    }

    @Override
    public String toString() {
        return String.format("SLAStatistics{total=%d, completed=%d, breached=%d, compliance=%.2f%%}",
                totalTasks, completedTasks, breachedTasks, slaComplianceRate);
    }

    public static class SLAStatisticsBuilder {
        private long totalTasks;
        private long completedTasks;
        private long breachedTasks;
        private Duration averageResponseTime = Duration.ZERO;
        private Duration averageResolutionTime = Duration.ZERO;
        private double slaComplianceRate = 100.0;

        public SLAStatisticsBuilder totalTasks(long totalTasks) {
            this.totalTasks = totalTasks;
            return this;
        }

        public SLAStatisticsBuilder completedTasks(long completedTasks) {
            this.completedTasks = completedTasks;
            return this;
        }

        public SLAStatisticsBuilder breachedTasks(long breachedTasks) {
            this.breachedTasks = breachedTasks;
            return this;
        }

        public SLAStatisticsBuilder averageResponseTime(Duration averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
            return this;
        }

        public SLAStatisticsBuilder averageResolutionTime(Duration averageResolutionTime) {
            this.averageResolutionTime = averageResolutionTime;
            return this;
        }

        public SLAStatisticsBuilder slaComplianceRate(double slaComplianceRate) {
            this.slaComplianceRate = slaComplianceRate;
            return this;
        }

        public SLAStatistics build() {
            return new SLAStatistics(
                    totalTasks,
                    completedTasks,
                    breachedTasks,
                    averageResponseTime,
                    averageResolutionTime,
                    slaComplianceRate
            );
        }
    }
}
