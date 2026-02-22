package com.huakuantong.iot.platform.device.application.service;

import com.huakuantong.iot.platform.device.domain.entity.DeviceTelemetry;
import com.huakuantong.iot.platform.device.domain.entity.DeviceEvent;
import com.huakuantong.iot.platform.device.domain.repository.DeviceTelemetryReadRepository;
import com.huakuantong.iot.platform.device.domain.repository.DeviceEventReadRepository;
import com.huakuantong.iot.platform.device.domain.repository.DeviceRepository;
import com.huakuantong.iot.platform.shared.domain.PageRequest;
import com.huakuantong.iot.platform.shared.domain.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 设备数据查询服务
 *
 * <p>统一入口，根据查询类型自动路由到合适的数据源：</p>
 * <ul>
 *   <li>当前状态查询 → MySQL</li>
 *   <li>历史趋势查询 → 时序数据库</li>
 *   <li>多设备聚合查询 → 时序数据库</li>
 *   <li>事件统计查询 → 时序数据库</li>
 * </ul>
 *
 * @author DDD Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceDataQueryService {

    // 写侧仓储（MySQL）
    private final DeviceRepository deviceRepository;

    // 读侧仓储（时序数据库）
    private final DeviceTelemetryReadRepository telemetryReadRepository;
    private final DeviceEventReadRepository eventReadRepository;

    // ==================== 当前状态查询（MySQL） ====================

    /**
     * 查询设备当前状态
     *
     * <p>从MySQL获取最新快照数据</p>
     *
     * @param deviceId 设备ID
     * @return 设备当前状态
     */
    public DeviceStatusDTO getDeviceStatus(String deviceId) {
        // 从MySQL查询设备聚合根
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        // 从最新快照构建状态DTO
        return DeviceStatusDTO.from(device);
    }

    /**
     * 批量查询设备当前状态
     *
     * @param deviceIds 设备ID列表
     * @return 设备状态列表
     */
    public List<DeviceStatusDTO> getDeviceStatusBatch(List<String> deviceIds) {
        return deviceIds.stream()
            .map(this::getDeviceStatus)
            .collect(Collectors.toList());
    }

    /**
     * 查询空间内所有设备状态
     *
     * @param spaceId 空间ID
     * @return 设备状态列表
     */
    public List<DeviceStatusDTO> getSpaceDeviceStatus(String spaceId) {
        // 从MySQL查询空间内的设备
        List<Device> devices = deviceRepository.findBySpaceId(spaceId);

        return devices.stream()
            .map(DeviceStatusDTO::from)
            .collect(Collectors.toList());
    }

    // ==================== 历史趋势查询（时序数据库） ====================

    /**
     * 查询设备历史遥测数据
     *
     * @param request 历史数据查询请求
     * @return 遥测数据列表
     */
    public List<DeviceTelemetry> getHistoricalTelemetry(HistoricalTelemetryQuery request) {
        log.debug("Query historical telemetry: deviceId={}, from={}, to={}",
            request.getDeviceId(), request.getFrom(), request.getTo());

        return telemetryReadRepository.findByDeviceIdAndTimeRange(
            request.getDeviceId(),
            request.getFrom(),
            request.getTo()
        );
    }

    /**
     * 分页查询设备历史遥测数据
     *
     * @param request    历史数据查询请求
     * @param pageRequest 分页请求
     * @return 分页结果
     */
    public PageResult<DeviceTelemetry> getHistoricalTelemetryPaged(
        HistoricalTelemetryQuery request,
        PageRequest pageRequest
    ) {
        return telemetryReadRepository.findByDeviceIdAndTimeRange(
            request.getDeviceId(),
            request.getFrom(),
            request.getTo(),
            pageRequest
        );
    }

    /**
     * 查询设备历史趋势（聚合数据）
     *
     * @param request  趋势查询请求
     * @return 趋势数据点列表
     */
    public List<TrendDataPoint> getTrend(TrendQuery request) {
        log.debug("Query trend: deviceId={}, metric={}, window={}",
            request.getDeviceId(), request.getMetricName(), request.getWindowSeconds());

        return telemetryReadRepository.aggregateMetricByWindow(
            request.getDeviceId(),
            request.getMetricName(),
            request.getFrom(),
            request.getTo(),
            request.getWindowSeconds(),
            request.getAggregateType()
        );
    }

    // ==================== 多设备聚合查询（时序数据库） ====================

    /**
     * 查询多个设备的最新遥测数据
     *
     * @param deviceIds 设备ID列表
     * @return 遥测数据列表
     */
    public List<DeviceTelemetry> getLatestTelemetryMulti(List<String> deviceIds) {
        return telemetryReadRepository.findLatestByDeviceIds(deviceIds);
    }

    /**
     * 查询空间内所有设备的最新遥测数据
     *
     * @param spaceId 空间ID
     * @return 遥测数据列表
     */
    public List<DeviceTelemetry> getSpaceLatestTelemetry(String spaceId) {
        return telemetryReadRepository.findLatestBySpaceId(spaceId);
    }

    /**
     * 计算空间内设备的指标统计
     *
     * @param request    统计请求
     * @return 统计结果
     */
    public SpaceMetricStatistics getSpaceMetricStatistics(SpaceMetricQuery request) {
        // 获取空间内所有设备的遥测数据
        List<DeviceTelemetry> telemetryList = telemetryReadRepository.findLatestBySpaceId(
            request.getSpaceId()
        );

        // 计算统计值
        return SpaceMetricStatistics.calculate(
            telemetryList,
            request.getMetricName()
        );
    }

    // ==================== 事件统计查询（时序数据库） ====================

    /**
     * 查询设备事件列表
     *
     * @param request 事件查询请求
     * @return 事件列表
     */
    public List<DeviceEvent> getDeviceEvents(DeviceEventQuery request) {
        return eventReadRepository.findByDeviceIdAndTimeRange(
            request.getDeviceId(),
            request.getFrom(),
            request.getTo()
        );
    }

    /**
     * 统计设备事件数量（按类型分组）
     *
     * @param request 事件统计请求
     * @return 事件类型->数量Map
     */
    public Map<String, Long> countDeviceEventsByType(DeviceEventStatisticsQuery request) {
        return eventReadRepository.countByEventType(
            request.getDeviceId(),
            request.getFrom(),
            request.getTo()
        );
    }

    /**
     * 统计设备事件数量（按级别分组）
     *
     * @param request 事件统计请求
     * @return 事件级别->数量Map
     */
    public Map<String, Long> countDeviceEventsByLevel(DeviceEventStatisticsQuery request) {
        return eventReadRepository.countByEventLevel(
            request.getDeviceId(),
            request.getFrom(),
            request.getTo()
        );
    }

    /**
     * 获取租户内未确认的告警事件
     *
     * @param tenantId 租户ID
     * @param limit    返回数量限制
     * @return 告警事件列表
     */
    public List<DeviceEvent> getUnacknowledgedAlarms(String tenantId, int limit) {
        return eventReadRepository.findUnacknowledgedAlarms(tenantId, limit);
    }

    // ==================== 数据质量查询 ====================

    /**
     * 计算设备数据完整率
     *
     * @param deviceId 设备ID
     * @param from     开始时间
     * @param to       结束时间
     * @return 完整率（0-1）
     */
    public double getDataIntegrity(String deviceId, LocalDateTime from, LocalDateTime to) {
        return telemetryReadRepository.calculateDataIntegrity(deviceId, from, to);
    }

    /**
     * 获取设备数据质量统计
     *
     * @param deviceId 设备ID
     * @param from     开始时间
     * @param to       结束时间
     * @return 数据质量统计
     */
    public DataQualityStatsDTO getDataQualityStats(
        String deviceId,
        LocalDateTime from,
        LocalDateTime to
    ) {
        DeviceTelemetryReadRepository.DataQualityStats stats =
            telemetryReadRepository.getDataQualityStats(deviceId, from, to);

        return DataQualityStatsDTO.from(stats);
    }

    // ==================== 综合查询 ====================

    /**
     * 获取设备概览
     *
     * <p>包含：当前状态、最新遥测数据、最近事件</p>
     *
     * @param deviceId 设备ID
     * @return 设备概览
     */
    public DeviceOverviewDTO getDeviceOverview(String deviceId) {
        // 1. 当前状态（MySQL）
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new DeviceNotFoundException(deviceId));

        // 2. 最新遥测数据（时序数据库）
        Optional<DeviceTelemetry> latestTelemetry =
            telemetryReadRepository.findLatestByDeviceId(deviceId);

        // 3. 最近事件（时序数据库）
        Optional<DeviceEvent> latestEvent = Optional.ofNullable(
            eventReadRepository.findLatestByDeviceId(deviceId)
        );

        return DeviceOverviewDTO.create(device, latestTelemetry, latestEvent);
    }

    // ==================== DTO类 ====================

    /**
     * 设备状态DTO
     */
    @Data
    public static class DeviceStatusDTO {
        private String deviceId;
        private String deviceName;
        private String deviceType;
        private String deviceStatus; // ONLINE/OFFLINE/DISABLED/FAULT
        private LocalDateTime lastOnlineTime;
        private Map<String, Object> latestProperties;
        private DataQuality dataQuality;

        public static DeviceStatusDTO from(Device device) {
            DeviceStatusDTO dto = new DeviceStatusDTO();
            dto.setDeviceId(device.getId().getValue());
            dto.setDeviceName(device.getDeviceName().getValue());
            dto.setDeviceType(device.getDeviceType().name());
            dto.setDeviceStatus(device.getDeviceStatus().name());
            dto.setLastOnlineTime(device.getLastOnlineTime());
            dto.setLatestProperties(device.getLatestProperties());
            // TODO: 设置数据质量
            return dto;
        }
    }

    /**
     * 趋势查询请求
     */
    @Data
    public static class TrendQuery {
        private String deviceId;
        private String metricName;
        private LocalDateTime from;
        private LocalDateTime to;
        private long windowSeconds = 3600; // 默认1小时
        private DeviceTelemetryReadRepository.AggregateType aggregateType =
            DeviceTelemetryReadRepository.AggregateType.MEAN;
    }

    /**
     * 趋势数据点
     */
    @Data
    @AllArgsConstructor
    public static class TrendDataPoint {
        private LocalDateTime timestamp;
        private Double value;
    }

    /**
     * 空间指标统计
     */
    @Data
    public static class SpaceMetricStatistics {
        private double avg;
        private double max;
        private double min;
        private long deviceCount;
        private Map<String, Double> deviceValues;

        public static SpaceMetricStatistics calculate(
            List<DeviceTelemetry> telemetryList,
            String metricName
        ) {
            // TODO: 实现统计计算
            return new SpaceMetricStatistics();
        }
    }

    /**
     * 设备概览DTO
     */
    @Data
    @Builder
    public static class DeviceOverviewDTO {
        private DeviceStatusDTO status;
        private DeviceTelemetry latestTelemetry;
        private DeviceEvent latestEvent;

        public static DeviceOverviewDTO create(
            Device device,
            Optional<DeviceTelemetry> telemetry,
            Optional<DeviceEvent> event
        ) {
            return DeviceOverviewDTO.builder()
                .status(DeviceStatusDTO.from(device))
                .latestTelemetry(telemetry.orElse(null))
                .latestEvent(event.orElse(null))
                .build();
        }
    }
}
