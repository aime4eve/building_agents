package com.hkt.iot.space.interfaces.rest.dto;

import com.hkt.iot.space.domain.model.Space;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 空间响应DTO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "空间响应")
public class SpaceResponse {

    @Schema(description = "空间ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "空间编码")
    private String spaceCode;

    @Schema(description = "空间名称")
    private String spaceName;

    @Schema(description = "空间类型")
    private Space.SpaceType spaceType;

    @Schema(description = "空间层级")
    private Integer spaceLevel;

    @Schema(description = "父空间ID")
    private Long parentSpaceId;

    @Schema(description = "根空间ID")
    private Long rootSpaceId;

    @Schema(description = "空间路径")
    private String spacePath;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区县")
    private String district;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "海拔")
    private BigDecimal altitude;

    @Schema(description = "边界坐标")
    private List<List<BigDecimal>> boundary;

    @Schema(description = "面积")
    private BigDecimal area;

    @Schema(description = "楼层号")
    private Integer floorNumber;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "容量")
    private Integer capacity;

    @Schema(description = "空间状态")
    private Space.SpaceStatus spaceStatus;

    @Schema(description = "使用状态")
    private Space.UsageStatus usageStatus;

    @Schema(description = "扩展属性")
    private Map<String, Object> extProperties;

    @Schema(description = "版本号")
    private Long version;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "创建人ID")
    private Long createdBy;

    @Schema(description = "更新人ID")
    private Long updatedBy;

    /**
     * 从领域模型转换为Response
     *
     * @param space 空间领域模型
     * @return 空间响应DTO
     */
    public static SpaceResponse from(Space space) {
        return SpaceResponse.builder()
                .id(space.getId())
                .tenantId(space.getTenantId())
                .spaceCode(space.getSpaceCode())
                .spaceName(space.getSpaceName())
                .spaceType(space.getSpaceType())
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
                .spaceStatus(space.getSpaceStatus())
                .usageStatus(space.getUsageStatus())
                .extProperties(space.getExtProperties())
                .version(space.getVersion())
                .createdAt(space.getCreatedAt())
                .updatedAt(space.getUpdatedAt())
                .createdBy(space.getCreatedBy())
                .updatedBy(space.getUpdatedBy())
                .build();
    }
}
