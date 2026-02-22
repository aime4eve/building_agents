package com.hkt.iot.space.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 创建空间请求DTO
 *
 * @author HKT IoT Team
 */
@Data
@Schema(description = "创建空间请求")
public class SpaceCreateRequest {

    @Schema(description = "租户ID", required = true)
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    @Schema(description = "空间编码", required = true, example = "SP001")
    @NotBlank(message = "空间编码不能为空")
    private String spaceCode;

    @Schema(description = "空间名称", required = true, example = "科技园A栋")
    @NotBlank(message = "空间名称不能为空")
    private String spaceName;

    @Schema(description = "空间类型", required = true, example = "BUILDING")
    @NotNull(message = "空间类型不能为空")
    private SpaceType spaceType;

    @Schema(description = "空间层级", required = true, example = "2")
    @NotNull(message = "空间层级不能为空")
    @Min(value = 1, message = "空间层级不能小于1")
    @Max(value = 4, message = "空间层级不能大于4")
    private Integer spaceLevel;

    @Schema(description = "父空间ID", example = "1")
    private Long parentSpaceId;

    @Schema(description = "地址", example = "深圳市南山区科技园")
    private String address;

    @Schema(description = "省份", example = "广东省")
    private String province;

    @Schema(description = "城市", example = "深圳市")
    private String city;

    @Schema(description = "区县", example = "南山区")
    private String district;

    @Schema(description = "经度", example = "114.057868")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "22.543099")
    private BigDecimal latitude;

    @Schema(description = "海拔(米)", example = "10.5")
    private BigDecimal altitude;

    @Schema(description = "边界坐标(多边形)")
    private List<List<BigDecimal>> boundary;

    @Schema(description = "面积(平方米)", example = "5000.50")
    private BigDecimal area;

    @Schema(description = "楼层号", example = "5")
    private Integer floorNumber;

    @Schema(description = "房间号", example = "501")
    private String roomNumber;

    @Schema(description = "容量", example = "100")
    private Integer capacity;

    @Schema(description = "扩展属性")
    private Map<String, Object> extProperties;

    /**
     * 空间类型枚举
     */
    public enum SpaceType {
        @Schema(description = "园区")
        PARK,
        @Schema(description = "建筑")
        BUILDING,
        @Schema(description = "楼层")
        FLOOR,
        @Schema(description = "房间")
        ROOM
    }
}
