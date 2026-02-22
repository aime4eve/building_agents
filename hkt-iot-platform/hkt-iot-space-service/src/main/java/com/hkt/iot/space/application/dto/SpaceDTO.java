package com.hkt.iot.space.application.dto;

import com.hkt.iot.space.domain.model.Space;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 空间DTO
 * 用于空间数据的传输
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceDTO {

    /**
     * 空间ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 空间编码
     */
    private String spaceCode;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间类型
     */
    private SpaceTypeDTO spaceType;

    /**
     * 空间层级
     */
    private Integer spaceLevel;

    /**
     * 父空间ID
     */
    private Long parentSpaceId;

    /**
     * 根空间ID
     */
    private Long rootSpaceId;

    /**
     * 空间路径
     */
    private String spacePath;

    /**
     * 地址
     */
    private String address;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 海拔
     */
    private BigDecimal altitude;

    /**
     * 边界坐标
     */
    private List<List<BigDecimal>> boundary;

    /**
     * 面积
     */
    private BigDecimal area;

    /**
     * 楼层号
     */
    private Integer floorNumber;

    /**
     * 房间号
     */
    private String roomNumber;

    /**
     * 容量
     */
    private Integer capacity;

    /**
     * 空间状态
     */
    private SpaceStatusDTO spaceStatus;

    /**
     * 使用状态
     */
    private UsageStatusDTO usageStatus;

    /**
     * 扩展属性
     */
    private Map<String, Object> extProperties;

    /**
     * 版本号
     */
    private Long version;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 空间类型枚举
     */
    public enum SpaceTypeDTO {
        PARK, BUILDING, FLOOR, ROOM
    }

    /**
     * 空间状态枚举
     */
    public enum SpaceStatusDTO {
        ACTIVE, INACTIVE, MAINTENANCE
    }

    /**
     * 使用状态枚举
     */
    public enum UsageStatusDTO {
        OCCUPIED, VACANT, RESERVED
    }

    /**
     * 从领域模型转换为DTO
     *
     * @param space 空间领域模型
     * @return 空间DTO
     */
    public static SpaceDTO from(Space space) {
        return SpaceDTO.builder()
                .id(space.getId())
                .tenantId(space.getTenantId())
                .spaceCode(space.getSpaceCode())
                .spaceName(space.getSpaceName())
                .spaceType(convertSpaceType(space.getSpaceType()))
                .spaceLevel(space.getSpaceLevel())
                .parentSpaceId(space.getParentSpaceId())
                .rootSpaceId(space.getRootSpaceId())
                .spacePath(space.getSpacePath())
                .address(space.getAddress())
                .province(space.getProvince())
                .city(space.getCity())
                .district(space.getDistrict())
                .longitude(space.getLongitude())
                .latitude(space.getLatitude())
                .altitude(space.getAltitude())
                .boundary(space.getBoundary())
                .area(space.getArea())
                .floorNumber(space.getFloorNumber())
                .roomNumber(space.getRoomNumber())
                .capacity(space.getCapacity())
                .spaceStatus(convertSpaceStatus(space.getSpaceStatus()))
                .usageStatus(convertUsageStatus(space.getUsageStatus()))
                .extProperties(space.getExtProperties())
                .version(space.getVersion())
                .createdAt(space.getCreatedAt())
                .updatedAt(space.getUpdatedAt())
                .createdBy(space.getCreatedBy())
                .updatedBy(space.getUpdatedBy())
                .build();
    }

    /**
     * 转换空间类型枚举
     */
    private static SpaceTypeDTO convertSpaceType(Space.SpaceType spaceType) {
        if (spaceType == null) {
            return null;
        }
        return SpaceTypeDTO.valueOf(spaceType.name());
    }

    /**
     * 转换空间状态枚举
     */
    private static SpaceStatusDTO convertSpaceStatus(Space.SpaceStatus spaceStatus) {
        if (spaceStatus == null) {
            return null;
        }
        return SpaceStatusDTO.valueOf(spaceStatus.name());
    }

    /**
     * 转换使用状态枚举
     */
    private static UsageStatusDTO convertUsageStatus(Space.UsageStatus usageStatus) {
        if (usageStatus == null) {
            return null;
        }
        return UsageStatusDTO.valueOf(usageStatus.name());
    }
}
