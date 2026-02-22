package com.hkt.iot.space.application.command;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 创建空间命令对象
 *
 * @author HKT IoT Team
 */
@Value
public class CreateSpaceCommand {

    /**
     * 租户ID
     */
    Long tenantId;

    /**
     * 空间编码
     */
    String spaceCode;

    /**
     * 空间名称
     */
    String spaceName;

    /**
     * 空间类型
     */
    SpaceTypeCommand spaceType;

    /**
     * 空间层级
     */
    Integer spaceLevel;

    /**
     * 父空间ID
     */
    Long parentSpaceId;

    /**
     * 地址
     */
    String address;

    /**
     * 省份
     */
    String province;

    /**
     * 城市
     */
    String city;

    /**
     * 区县
     */
    String district;

    /**
     * 经度
     */
    BigDecimal longitude;

    /**
     * 纬度
     */
    BigDecimal latitude;

    /**
     * 海拔
     */
    BigDecimal altitude;

    /**
     * 边界坐标
     */
    List<List<BigDecimal>> boundary;

    /**
     * 面积
     */
    BigDecimal area;

    /**
     * 楼层号
     */
    Integer floorNumber;

    /**
     * 房间号
     */
    String roomNumber;

    /**
     * 容量
     */
    Integer capacity;

    /**
     * 扩展属性
     */
    Map<String, Object> extProperties;

    /**
     * 创建人ID
     */
    Long createdBy;

    /**
     * 空间类型枚举
     */
    public enum SpaceTypeCommand {
        PARK, BUILDING, FLOOR, ROOM
    }

    /**
     * 验证命令对象
     */
    public void validate() {
        if (tenantId == null) {
            throw new IllegalArgumentException("租户ID不能为空");
        }
        if (spaceCode == null || spaceCode.trim().isEmpty()) {
            throw new IllegalArgumentException("空间编码不能为空");
        }
        if (spaceName == null || spaceName.trim().isEmpty()) {
            throw new IllegalArgumentException("空间名称不能为空");
        }
        if (spaceType == null) {
            throw new IllegalArgumentException("空间类型不能为空");
        }
        if (spaceLevel == null || spaceLevel < 1 || spaceLevel > 4) {
            throw new IllegalArgumentException("空间层级必须在1-4之间");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("创建人ID不能为空");
        }
    }
}
