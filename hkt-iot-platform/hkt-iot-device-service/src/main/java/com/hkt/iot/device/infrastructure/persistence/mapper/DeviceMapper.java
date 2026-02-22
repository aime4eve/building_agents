package com.hkt.iot.device.infrastructure.persistence.mapper;

import com.hkt.iot.device.domain.model.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Device Mapper接口
 * 基于DDL: device表，使用MyBatis实现复杂查询
 *
 * @author HKT IoT Team
 */
@Mapper
public interface DeviceMapper {

    /**
     * 查询设备详情（含关联信息）
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    Device selectDeviceDetail(@Param("deviceId") Long deviceId);

    /**
     * 分页查询设备列表
     *
     * @param tenantId    租户ID
     * @param deviceType  设备类型
     * @param deviceStatus 设备状态
     * @param onlineStatus 在线状态
     * @param spaceId     空间ID
     * @param keyword     关键字
     * @param offset      偏移量
     * @param limit       限制数量
     * @return 设备列表
     */
    List<Device> selectDevicesPage(
            @Param("tenantId") Long tenantId,
            @Param("deviceType") String deviceType,
            @Param("deviceStatus") String deviceStatus,
            @Param("onlineStatus") Boolean onlineStatus,
            @Param("spaceId") Long spaceId,
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    /**
     * 统计设备数量
     *
     * @param tenantId     租户ID
     * @param deviceType   设备类型
     * @param deviceStatus 设备状态
     * @param onlineStatus 在线状态
     * @param spaceId      空间ID
     * @return 设备数量
     */
    long countDevices(
            @Param("tenantId") Long tenantId,
            @Param("deviceType") String deviceType,
            @Param("deviceStatus") String deviceStatus,
            @Param("onlineStatus") Boolean onlineStatus,
            @Param("spaceId") Long spaceId
    );

    /**
     * 查询设备在线状态统计
     *
     * @param tenantId 租户ID
     * @return 状态统计列表
     */
    List<Map<String, Object>> selectDeviceStatusStats(@Param("tenantId") Long tenantId);

    /**
     * 查询设备类型统计
     *
     * @param tenantId 租户ID
     * @return 类型统计列表
     */
    List<Map<String, Object>> selectDeviceTypeStats(@Param("tenantId") Long tenantId);
}
