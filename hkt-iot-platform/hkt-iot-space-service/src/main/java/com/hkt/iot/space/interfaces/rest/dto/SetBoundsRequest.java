package com.hkt.iot.space.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 设置边界请求
 * 用于设置空间的地理坐标边界范围
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设置边界请求")
public class SetBoundsRequest {

    @NotNull(message = "东北角纬度不能为空")
    @DecimalMin(value = "-90", message = "纬度必须在-90到90之间")
    @DecimalMax(value = "90", message = "纬度必须在-90到90之间")
    @Schema(description = "东北角纬度", required = true)
    private BigDecimal northeastLatitude;

    @NotNull(message = "东北角经度不能为空")
    @DecimalMin(value = "-180", message = "经度必须在-180到180之间")
    @DecimalMax(value = "180", message = "经度必须在-180到180之间")
    @Schema(description = "东北角经度", required = true)
    private BigDecimal northeastLongitude;

    @NotNull(message = "西南角纬度不能为空")
    @DecimalMin(value = "-90", message = "纬度必须在-90到90之间")
    @DecimalMax(value = "90", message = "纬度必须在-90到90之间")
    @Schema(description = "西南角纬度", required = true)
    private BigDecimal southwestLatitude;

    @NotNull(message = "西南角经度不能为空")
    @DecimalMin(value = "-180", message = "经度必须在-180到180之间")
    @DecimalMax(value = "180", message = "经度必须在-180到180之间")
    @Schema(description = "西南角经度", required = true)
    private BigDecimal southwestLongitude;
}
