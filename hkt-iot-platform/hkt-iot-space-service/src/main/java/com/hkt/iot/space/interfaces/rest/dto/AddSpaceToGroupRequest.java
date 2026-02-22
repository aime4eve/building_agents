package com.hkt.iot.space.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加空间到分组请求DTO
 *
 * @author HKT IoT Team
 */
@Data
@Schema(description = "添加空间到分组请求")
public class AddSpaceToGroupRequest {

    @Schema(description = "租户ID", required = true)
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    @Schema(description = "空间编码", required = true)
    @NotNull(message = "空间编码不能为空")
    private String spaceCode;

    @Schema(description = "空间名称")
    private String spaceName;

    @Schema(description = "成员顺序")
    private Integer memberOrder;

    @Schema(description = "是否置顶")
    private Boolean isPinned = false;
}
