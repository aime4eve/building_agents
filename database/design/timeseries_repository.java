package com.huakuantong.iot.platform.device.domain.repository;

import com.huakuantong.iot.platform.device.domain.aggregate.Device;
import com.huakuantong.iot.platform.device.domain.entity.DeviceTelemetry;
import com.huakuantong.iot.platform.device.domain.entity.DeviceEvent;
import com.huakuantong.iot.platform.shared.domain.PageRequest;
import com.huakuantong.iot.platform.shared.domain.PageResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 设备遥测数据读仓储接口
 *
 * <p>面向时序数据库查询，支持：</p>
 * <ul>
 *   <li>查询设备最新遥测数据</li>
 *   <li>按时间范围查询历史数据</li>
 *   <li>聚合统计查询</li>
 *   <li>多设备数据查询</li>
 * </ul>
 *
 * <p>实现类可基于 InfluxDB 或 TDengine</p>
 *
 * @author DDD Team
 * @version 1.0
 */
public interface DeviceTelemetryReadRepository {

    // ==================== 单设备查询 ====================

    /**
     * 查询设备最新遥测数据
     *
     * @param deviceId 设备ID
     * @return 最新遥测数据，不存在返回空
     */
    Optional<DeviceTelemetry> findLatestByDeviceId(String deviceId);

    /**
     * 查询设备在指定时间点的遥测数据
     *
     * @param deviceId   设备ID
     * @param timestamp 时间点
     * @return 遥测数据，不存在返回空
     */
    Optional<DeviceTelemetry> findByDeviceIdAndTimestamp(String deviceId, LocalDateTime timestamp);

    /**
     * 查询设备在指定时间范围的遥测数据
     *
     * @param deviceId 设备ID
     * @param from     开始时间
     * @param to       结束时间
     * @return 遥测数据列表，按时间升序
     */
    List<DeviceTelemetry> findByDeviceIdAndTimeRange(String deviceId, LocalDateTime from, LocalDateTime to);

    /**
     * 分页查询设备在指定时间范围的遥测数据
     *
     * @param deviceId    设备ID
     * @param from        开始时间
     * @param to          结束时间
     * @param pageRequest 分页请求
     * @return 分页结果
     */
    PageResult<DeviceTelemetry> findByDeviceIdAndTimeRange(
        String deviceId,
        LocalDateTime from,
        LocalDateTime to,
        PageRequest pageRequest
    );

    // ==================== 指标查询 ====================

    /**
     * 查询设备指定指标的最新值
     *
     * @param deviceId    设备ID
     * @param metricName 指标名称（如 temperature, humidity）
     * @return 指标值，不存在返回空
     */
    Optional<Double> findLatestMetricValue(String deviceId, String metricName);

    /**
     * 查询设备指定指标在时间范围的数据
     *
     * @param deviceId    设备ID
     * @param metricName 指标名称
     * @param from        开始时间
     * @param to          结束时间
     * @return 时间值对列表
     */
    List<MetricPoint> findMetricByTimeRange(String deviceId, String metricName, LocalDateTime from, LocalDateTime to);

    // ==================== 多设备查询 ====================

    /**
     * 查询多个设备的最新遥测数据
     *
     * @param deviceIds 设备ID列表
     * @return 设备遥测数据Map，key为设备ID
     */
    List<DeviceTelemetry> findLatestByDeviceIds(List<String> deviceIds);

    /**
     * 查询空间下所有设备的最新遥测数据
     *
     * @param spaceId 空间ID
     * @return 设备遥测数据列表
     */
    List<DeviceTelemetry> findLatestBySpaceId(String spaceId);

    /**
     * 查询租户下所有设备的最新遥测数据
     *
     * @param tenantId 租户ID
     * @return 设备遥测数据列表
     */
    List<DeviceTelemetry> findLatestByTenantId(String tenantId);

    // ==================== 聚合统计查询 ====================

    /**
     * 计算设备指标在时间范围内的统计值
     *
     * @param deviceId    设备ID
     * @param metricName 指标名称
     * @param from        开始时间
     * @param to          结束时间
     * @param aggregate   聚合类型：MEAN, MAX, MIN, SUM, COUNT
     * @return 统计值
     */
    Optional<Double> aggregateMetric(
        String deviceId,
        String metricName,
        LocalDateTime from,
        LocalDateTime to,
        AggregateType aggregate
    );

    /**
     * 按时间窗口聚合设备指标数据
     *
     * @param deviceId    设备ID
     * @param metricName 指标名称
     * @param from        开始时间
     * @param to          结束时间
     * @param window      时间窗口（秒）
     * @param aggregate   聚合类型
     * @return 时间窗口聚合结果列表
     */
    List<MetricPoint> aggregateMetricByWindow(
        String deviceId,
        String metricName,
        LocalDateTime from,
        LocalDateTime to,
        long window,
        AggregateType aggregate
    );

    /**
     * 计算设备数据上报频率
     *
     * @param deviceId 设备ID
     * @param from     开始时间
     * @param to       结束时间
     * @return 每秒上报次数
     */
    double calculateReportFrequency(String deviceId, LocalDateTime from, LocalDateTime to);

    // ==================== 数据质量查询 ====================

    /**
     * 查询设备数据完整率
     *
     * @param deviceId 设备ID
     * @param from     开始时间
     * @param to       结束时间
     * @return 完整率（0-1）
     */
    double calculateDataIntegrity(String deviceId, LocalDateTime from, LocalDateTime to);

    /**
     * 查询设备数据质量分布
     *
     * @param deviceId 设备ID
     * @param from     开始时间
     * @param to       结束时间
     * @return 数据质量统计
     */
    DataQualityStats getDataQualityStats(String deviceId, LocalDateTime from, LocalDateTime to);

    // ==================== 历史数据清理 ====================

    /**
     * 删除设备指定时间范围的历史数据
     *
     * @param deviceId 设备ID
     * @param before   删除此时间之前的数据
     * @return 删除的记录数
     */
    long deleteBefore(String deviceId, LocalDateTime before);

    // ==================== 内部类/枚举 ====================

    /**
     * 聚合类型
     */
    enum AggregateType {
        MEAN,   // 平均值
        MAX,    // 最大值
        MIN,    // 最小值
        SUM,    // 求和
        COUNT,  // 计数
        FIRST,  // 首值
        LAST    // 末值
    }

    /**
     * 指标点（时间-值对）
     */
    class MetricPoint {
        private final LocalDateTime timestamp;
        private final Double value;

        public MetricPoint(LocalDateTime timestamp, Double value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public Double getValue() {
            return value;
        }
    }

    /**
     * 数据质量统计
     */
    class DataQualityStats {
        private final long totalCount;
        private final long goodCount;
        private final long fairCount;
        private final long poorCount;
        private final double goodRate;

        public DataQualityStats(long totalCount, long goodCount, long fairCount, long poorCount) {
            this.totalCount = totalCount;
            this.goodCount = goodCount;
            this.fairCount = fairCount;
            this.poorCount = poorCount;
            this.goodRate = totalCount > 0 ? (double) goodCount / totalCount : 0;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public long getGoodCount() {
            return goodCount;
        }

        public long getFairCount() {
            return fairCount;
        }

        public long getPoorCount() {
            return poorCount;
        }

        public double getGoodRate() {
            return goodRate;
        }
    }
}
