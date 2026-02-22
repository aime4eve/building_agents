package com.huakuantong.iot.platform.device.domain.repository;

import com.huakuantong.iot.platform.device.domain.entity.DeviceEvent;
import com.huakuantong.iot.platform.shared.domain.PageRequest;
import com.huakuantong.iot.platform.shared.domain.PageResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备事件读仓储接口
 *
 * <p>面向时序数据库查询，支持：</p>
 * <ul>
 *   <li>查询设备最新事件</li>
 *   <li>按时间范围查询历史事件</li>
 *   <li>按事件类型/级别查询</li>
 *   <li>事件统计分析</li>
 * </ul>
 *
 * <p>实现类可基于 InfluxDB 或 TDengine</p>
 *
 * @author DDD Team
 * @version 1.0
 */
public interface DeviceEventReadRepository {

    // ==================== 单设备事件查询 ====================

    /**
     * 查询设备最新事件
     *
     * @param deviceId 设备ID
     * @return 最新事件，不存在返回空
     */
    default DeviceEvent findLatestByDeviceId(String deviceId) {
        List<DeviceEvent> events = findLatestByDeviceIds(List.of(deviceId));
        return events.isEmpty() ? null : events.get(0);
    }

    /**
     * 查询设备在指定时间范围的事件
     *
     * @param deviceId 设备ID
     * @param from     开始时间
     * @param to       结束时间
     * @return 事件列表，按时间降序
     */
    List<DeviceEvent> findByDeviceIdAndTimeRange(String deviceId, LocalDateTime from, LocalDateTime to);

    /**
     * 分页查询设备在指定时间范围的事件
     *
     * @param deviceId    设备ID
     * @param from        开始时间
     * @param to          结束时间
     * @param pageRequest 分页请求
     * @return 分页结果
     */
    PageResult<DeviceEvent> findByDeviceIdAndTimeRange(
        String deviceId,
        LocalDateTime from,
        LocalDateTime to,
        PageRequest pageRequest
    );

    // ==================== 按类型/级别查询 ====================

    /**
     * 查询设备指定类型的事件
     *
     * @param deviceId  设备ID
     * @param eventType 事件类型（ONLINE/OFFLINE/FAULT/ALERT等）
     * @param from      开始时间
     * @param to        结束时间
     * @return 事件列表
     */
    List<DeviceEvent> findByDeviceIdAndEventType(
        String deviceId,
        String eventType,
        LocalDateTime from,
        LocalDateTime to
    );

    /**
     * 查询设备指定级别的事件
     *
     * @param deviceId    设备ID
     * @param eventLevel 事件级别（INFO/WARNING/ERROR/CRITICAL）
     * @param from        开始时间
     * @param to          结束时间
     * @return 事件列表
     */
    List<DeviceEvent> findByDeviceIdAndEventLevel(
        String deviceId,
        String eventLevel,
        LocalDateTime from,
        LocalDateTime to
    );

    // ==================== 多设备事件查询 ====================

    /**
     * 查询多个设备的最新事件
     *
     * @param deviceIds 设备ID列表
     * @return 设备事件列表
     */
    List<DeviceEvent> findLatestByDeviceIds(List<String> deviceIds);

    /**
     * 查询空间下所有设备的最新事件
     *
     * @param spaceId 空间ID
     * @return 事件列表
     */
    List<DeviceEvent> findLatestBySpaceId(String spaceId);

    /**
     * 查询租户下所有设备的最新事件
     *
     * @param tenantId 租户ID
     * @param limit    返回数量限制
     * @return 事件列表
     */
    List<DeviceEvent> findLatestByTenantId(String tenantId, int limit);

    /**
     * 查询租户下指定级别的事件
     *
     * @param tenantId   租户ID
     * @param eventLevel 事件级别
     * @param from       开始时间
     * @param to         结束时间
     * @param limit      返回数量限制
     * @return 事件列表
     */
    List<DeviceEvent> findByTenantIdAndEventLevel(
        String tenantId,
        String eventLevel,
        LocalDateTime from,
        LocalDateTime to,
        int limit
    );

    // ==================== 事件统计查询 ====================

    /**
     * 统计设备在时间范围内的事件数量（按类型分组）
     *
     * @param deviceId 设备ID
     * @param from     开始时间
     * @param to       结束时间
     * @return 事件类型->数量的Map
     */
    Map<String, Long> countByEventType(String deviceId, LocalDateTime from, LocalDateTime to);

    /**
     * 统计设备在时间范围内的事件数量（按级别分组）
     *
     * @param deviceId 设备ID
     * @param from     开始时间
     * @param to       结束时间
     * @return 事件级别->数量的Map
     */
    Map<String, Long> countByEventLevel(String deviceId, LocalDateTime from, LocalDateTime to);

    /**
     * 统计空间内设备的事件数量
     *
     * @param spaceId 空间ID
     * @param from    开始时间
     * @param to      结束时间
     * @return 事件总数
     */
    long countBySpaceId(String spaceId, LocalDateTime from, LocalDateTime to);

    /**
     * 统计租户内设备的事件数量（按类型和级别分组）
     *
     * @param tenantId 租户ID
     * @param from     开始时间
     * @param to       结束时间
     * @return 统计结果列表
     */
    List<EventStatistics> statisticsByTenantId(String tenantId, LocalDateTime from, LocalDateTime to);

    // ==================== 告警事件查询 ====================

    /**
     * 查询未确认的告警事件
     *
     * @param tenantId 租户ID
     * @param limit    返回数量限制
     * @return 告警事件列表
     */
    List<DeviceEvent> findUnacknowledgedAlarms(String tenantId, int limit);

    /**
     * 查询设备的告警频率
     *
     * @param deviceId 设备ID
     * @param from     开始时间
     * @param to       结束时间
     * @return 告警事件列表
     */
    List<DeviceEvent> findAlarmEvents(String deviceId, LocalDateTime from, LocalDateTime to);

    // ==================== 事件历史清理 ====================

    /**
     * 删除设备指定时间之前的历史事件
     *
     * @param deviceId 设备ID
     * @param before   删除此时间之前的事件
     * @return 删除的记录数
     */
    long deleteBefore(String deviceId, LocalDateTime before);

    // ==================== 内部类 ====================

    /**
     * 事件统计结果
     */
    class EventStatistics {
        private final String eventType;
        private final String eventLevel;
        private final long count;

        public EventStatistics(String eventType, String eventLevel, long count) {
            this.eventType = eventType;
            this.eventLevel = eventLevel;
            this.count = count;
        }

        public String getEventType() {
            return eventType;
        }

        public String getEventLevel() {
            return eventLevel;
        }

        public long getCount() {
            return count;
        }
    }
}
