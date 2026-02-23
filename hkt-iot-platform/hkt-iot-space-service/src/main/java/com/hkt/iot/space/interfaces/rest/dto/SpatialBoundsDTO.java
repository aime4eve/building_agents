package com.hkt.iot.space.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 空间边界DTO
 * 表示地球表面上的矩形区域边界（由东北角和西南角坐标定义）
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "空间边界")
public class SpatialBoundsDTO {

    @Schema(description = "东北角纬度")
    private BigDecimal northeastLatitude;

    @Schema(description = "东北角经度")
    private BigDecimal northeastLongitude;

    @Schema(description = "西南角纬度")
    private BigDecimal southwestLatitude;

    @Schema(description = "西南角经度")
    private BigDecimal southwestLongitude;
}
