package com.hkt.iot.space.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新逻辑空间分组请求DTO
 *
 * @author HKT IoT Team
 */
@Data
@Schema(description = "更新逻辑空间分组请求")
public class LogicalSpaceGroupUpdateRequest {

    @Schema(description = "分组名称", example = "办公区域")
    private String groupName;

    @Schema(description = "分组描述", example = "办公区域分组")
    private String description;

    @Schema(description = "分组颜色", example = "#FF5722")
    private String groupColor;

    @Schema(description = "分组图标", example = "office")
    private String groupIcon;

    @Schema(description = "显示顺序", example = "1")
    private Integer displayOrder;

    @Schema(description = "版本号(乐观锁)", required = true)
    @NotNull(message = "版本号不能为空")
    private Long version;
}
