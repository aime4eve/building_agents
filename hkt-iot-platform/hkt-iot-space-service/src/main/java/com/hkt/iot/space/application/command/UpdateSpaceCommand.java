package com.hkt.iot.space.application.command;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 更新空间命令对象
 *
 * @author HKT IoT Team
 */
@Value
public class UpdateSpaceCommand {

    /**
     * 空间ID
     */
    Long spaceId;

    /**
     * 空间名称
     */
    String spaceName;

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
     * 更新人ID
     */
    Long updatedBy;

    /**
     * 版本号（乐观锁）
     */
    Long version;

    /**
     * 验证命令对象
     */
    public void validate() {
        if (spaceId == null) {
            throw new IllegalArgumentException("空间ID不能为空");
        }
        if (spaceName == null || spaceName.trim().isEmpty()) {
            throw new IllegalArgumentException("空间名称不能为空");
        }
        if (updatedBy == null) {
            throw new IllegalArgumentException("更新人ID不能为空");
        }
        if (version == null) {
            throw new IllegalArgumentException("版本号不能为空");
        }
    }
}
