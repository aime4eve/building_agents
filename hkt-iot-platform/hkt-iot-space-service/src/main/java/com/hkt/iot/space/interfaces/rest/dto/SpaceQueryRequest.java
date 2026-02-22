package com.hkt.iot.space.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 空间查询请求DTO
 *
 * @author HKT IoT Team
 */
@Data
@Schema(description = "空间查询请求")
public class SpaceQueryRequest {

    @Schema(description = "租户ID", example = "1")
    private Long tenantId;

    @Schema(description = "空间编码", example = "SP001")
    private String spaceCode;

    @Schema(description = "空间名称(模糊查询)", example = "科技园")
    private String spaceName;

    @Schema(description = "空间类型", example = "BUILDING")
    private SpaceType spaceType;

    @Schema(description = "父空间ID", example = "1")
    private Long parentSpaceId;

    @Schema(description = "根空间ID", example = "1")
    private Long rootSpaceId;

    @Schema(description = "空间状态", example = "ACTIVE")
    private SpaceStatus spaceStatus;

    @Schema(description = "使用状态", example = "VACANT")
    private UsageStatus usageStatus;

    @Schema(description = "省份", example = "广东省")
    private String province;

    @Schema(description = "城市", example = "深圳市")
    private String city;

    @Schema(description = "区县", example = "南山区")
    private String district;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "页大小", example = "20")
    private Integer pageSize = 20;

    /**
     * 空间类型枚举
     */
    public enum SpaceType {
        PARK, BUILDING, FLOOR, ROOM
    }

    /**
     * 空间状态枚举
     */
    public enum SpaceStatus {
        ACTIVE, INACTIVE, MAINTENANCE
    }

    /**
     * 使用状态枚举
     */
    public enum UsageStatus {
        OCCUPIED, VACANT, RESERVED
    }
}
