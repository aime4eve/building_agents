package com.hkt.iot.device.domain.repository;

import com.hkt.iot.device.domain.model.TelemetryData;
import com.hkt.iot.device.domain.model.TelemetryData.DataType;
import com.hkt.iot.domain.repository.BaseRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 遥测数据仓储接口
 * 由于遥测数据量大，使用时序数据库存储
 *
 * @author HKT IoT Team
 */
public interface TelemetryDataRepository extends BaseRepository<TelemetryData, Long> {

    /**
     * 批量保存遥测数据
     *
     * @param telemetryDataList 遥测数据列表
     */
    void batchSave(List<TelemetryData> telemetryDataList);

    /**
     * 查询设备的最新遥测数据
     *
     * @param deviceId 设备ID
     * @param dataKey  数据键
     * @return 遥测数据
     */
    List<TelemetryData> findLatestByDeviceIdAndDataKey(Long deviceId, String dataKey);

    /**
     * 查询设备在时间范围内的遥测数据
     *
     * @param deviceId 设备ID
     * @param dataKey  数据键
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 遥测数据列表
     */
    List<TelemetryData> findByDeviceIdAndDataKeyAndDataTimeBetween(
            Long deviceId,
            String dataKey,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    /**
     * 查询设备在时间范围内的所有遥测数据
     *
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 遥测数据列表
     */
    List<TelemetryData> findByDeviceIdAndDataTimeBetween(
            Long deviceId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    /**
     * 按批次ID查询遥测数据
     *
     * @param batchId 批次ID
     * @return 遥测数据列表
     */
    List<TelemetryData> findByBatchId(String batchId);

    /**
     * 删除过期数据
     *
     * @param beforeTime 时间阈值
     * @return 删除数量
     */
    long deleteByDataTimeBefore(LocalDateTime beforeTime);

    /**
     * 统计遥测数据量
     *
     * @param tenantId   租户ID
     * @param deviceId   设备ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 数据量
     */
    long countByTenantIdAndDeviceIdAndDataTimeBetween(
            Long tenantId,
            Long deviceId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}
