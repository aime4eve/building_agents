package com.hkt.iot.device.application.service;

import com.hkt.iot.device.domain.event.TelemetryReceivedEvent;
import com.hkt.iot.device.domain.model.Device;
import com.hkt.iot.device.domain.model.TelemetryData;
import com.hkt.iot.device.domain.repository.DeviceRepository;
import com.hkt.iot.device.domain.repository.TelemetryDataRepository;
import com.hkt.iot.device.application.event.DeviceEventPublisher;
import com.hkt.iot.device.infrastructure.timeseries.InfluxDBService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 遥测数据应用服务
 * 负责遥测数据的接收、聚合与存储
 * 写侧：快照存储到关系数据库
 * 读侧：时序数据存储到InfluxDB
 *
 * @author HKT IoT Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryService {

    private final TelemetryDataRepository telemetryDataRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceEventPublisher eventPublisher;
    private final InfluxDBService influxDBService;

    /**
     * 接收设备遥测数据（单条）
     */
    @Transactional(rollbackFor = Exception.class)
    public void receiveTelemetry(
            Long tenantId,
            Long deviceId,
            String deviceSn,
            Map<String, Object> data,
            LocalDateTime dataTime,
            String eventId) {

        log.debug("接收遥测数据: deviceId={}, dataKeys={}, dataTime={}",
                deviceId, data.keySet(), dataTime);

        // 查询设备
        Device device = deviceRepository.findById(deviceId)
                .orElseGet(() -> {
                    log.warn("设备不存在，跳过处理: deviceId={}", deviceId);
                    return null;
                });

        if (device == null) {
            return;
        }

        // 更新设备遥测快照
        device.updateTelemetrySnapshot(data);
        device.setLastDataTime(LocalDateTime.now());
        deviceRepository.save(device);

        // 存储遥测数据（写侧快照）
        String batchId = UUID.randomUUID().toString();
        List<TelemetryData> telemetryList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            TelemetryData telemetry = TelemetryData.create(
                    tenantId,
                    deviceId,
                    deviceSn,
                    TelemetryData.DataType.PROPERTY,
                    entry.getKey(),
                    entry.getValue(),
                    null,  // unit
                    dataTime != null ? dataTime : LocalDateTime.now(),
                    TelemetryData.QualityCode.GOOD,
                    eventId,
                    batchId
            );
            telemetryList.add(telemetry);
        }

        // 批量保存到关系数据库
        telemetryDataRepository.batchSave(telemetryList);

        // 异步写入时序数据库（读侧时序模型）
        try {
            influxDBService.writeTelemetry(deviceSn, data, dataTime);
        } catch (Exception e) {
            log.error("写入时序数据库失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            // 不影响主流程，时序数据写入失败可容忍
        }

        // 发布遥测接收事件
        TelemetryReceivedEvent event = new TelemetryReceivedEvent(
                telemetryList.get(0).getId(),
                deviceId,
                deviceSn,
                tenantId,
                TelemetryData.DataType.PROPERTY,
                data,
                dataTime != null ? dataTime : LocalDateTime.now(),
                LocalDateTime.now(),
                eventId,
                batchId
        );
        eventPublisher.publishEvent(event);

        log.debug("遥测数据处理完成: deviceId={}, dataCount={}", deviceId, data.size());
    }

    /**
     * 批量接收遥测数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void receiveTelemetryBatch(List<TelemetryBatchItem> batchItems) {
        if (batchItems == null || batchItems.isEmpty()) {
            return;
        }

        log.info("批量接收遥测数据: count={}", batchItems.size());

        String batchId = UUID.randomUUID().toString();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (TelemetryBatchItem item : batchItems) {
            try {
                receiveTelemetry(
                        item.getTenantId(),
                        item.getDeviceId(),
                        item.getDeviceSn(),
                        item.getData(),
                        item.getDataTime(),
                        item.getEventId()
                );
                successCount.incrementAndGet();
            } catch (Exception e) {
                log.error("处理遥测数据失败: deviceId={}, error={}",
                        item.getDeviceId(), e.getMessage(), e);
                failureCount.incrementAndGet();
            }
        }

        log.info("批量遥测数据处理完成: total={}, success={}, failure={}",
                batchItems.size(), successCount.get(), failureCount.get());
    }

    /**
     * 查询设备最新遥测数据
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getLatestTelemetry(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));

        return device.getLatestProperties();
    }

    /**
     * 查询设备历史遥测数据（从时序数据库）
     */
    public List<Map<String, Object>> getHistoricalTelemetry(
            String deviceSn,
            String dataKey,
            LocalDateTime startTime,
            LocalDateTime endTime,
            long limit) {

        log.debug("查询历史遥测数据: deviceSn={}, dataKey={}, startTime={}, endTime={}",
                deviceSn, dataKey, startTime, endTime);

        return influxDBService.queryTelemetry(deviceSn, dataKey, startTime, endTime, limit);
    }

    /**
     * 查询设备多个数据点的历史数据
     */
    public Map<String, List<Map<String, Object>>> getMultiHistoricalTelemetry(
            String deviceSn,
            List<String> dataKeys,
            LocalDateTime startTime,
            LocalDateTime endTime,
            long limit) {

        log.debug("查询多点位历史遥测数据: deviceSn={}, dataKeys={}", deviceSn, dataKeys);

        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        for (String dataKey : dataKeys) {
            List<Map<String, Object>> data = influxDBService.queryTelemetry(
                    deviceSn, dataKey, startTime, endTime, limit);
            result.put(dataKey, data);
        }

        return result;
    }

    /**
     * 聚合遥测数据（按时间窗口）
     */
    public Map<String, Object> aggregateTelemetry(
            String deviceSn,
            String dataKey,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String aggregationType) {

        log.debug("聚合遥测数据: deviceSn={}, dataKey={}, aggregationType={}",
                deviceSn, dataKey, aggregationType);

        return influxDBService.aggregateTelemetry(
                deviceSn, dataKey, startTime, endTime, aggregationType);
    }

    /**
     * 清理过期遥测数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void cleanupExpiredTelemetry(LocalDateTime beforeTime) {
        log.info("清理过期遥测数据: beforeTime={}", beforeTime);

        long deletedCount = telemetryDataRepository.deleteByDataTimeBefore(beforeTime);
        log.info("过期遥测数据清理完成: deletedCount={}", deletedCount);
    }

    /**
     * 遥测批量项
     */
    public static class TelemetryBatchItem {
        private final Long tenantId;
        private final Long deviceId;
        private final String deviceSn;
        private final Map<String, Object> data;
        private final LocalDateTime dataTime;
        private final String eventId;

        public TelemetryBatchItem(Long tenantId, Long deviceId, String deviceSn,
                                   Map<String, Object> data, LocalDateTime dataTime, String eventId) {
            this.tenantId = tenantId;
            this.deviceId = deviceId;
            this.deviceSn = deviceSn;
            this.data = data;
            this.dataTime = dataTime;
            this.eventId = eventId;
        }

        public Long getTenantId() { return tenantId; }
        public Long getDeviceId() { return deviceId; }
        public String getDeviceSn() { return deviceSn; }
        public Map<String, Object> getData() { return data; }
        public LocalDateTime getDataTime() { return dataTime; }
        public String getEventId() { return eventId; }
    }
}
