package com.hkt.iot.space.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 创建逻辑空间分组请求DTO
 *
 * @author HKT IoT Team
 */
@Data
@Schema(description = "创建逻辑空间分组请求")
public class LogicalSpaceGroupCreateRequest {

    @Schema(description = "租户ID", required = true)
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    @Schema(description = "分组编码", required = true, example = "GROUP001")
    @NotBlank(message = "分组编码不能为空")
    private String groupCode;

    @Schema(description = "分组名称", required = true, example = "办公区域")
    @NotBlank(message = "分组名称不能为空")
    private String groupName;

    @Schema(description = "分组类型", required = true)
    @NotNull(message = "分组类型不能为空")
    private GroupType groupType;

    @Schema(description = "分组描述", example = "办公区域分组")
    private String description;

    @Schema(description = "分组颜色", example = "#FF5722")
    private String groupColor;

    @Schema(description = "分组图标", example = "office")
    private String groupIcon;

    @Schema(description = "分组规则")
    private Map<String, Object> groupRule;

    @Schema(description = "显示顺序", example = "1")
    private Integer displayOrder;

    /**
     * 分组类型枚举
     */
    public enum GroupType {
        @Schema(description = "应用分组")
        APPLICATION,
        @Schema(description = "租户分组")
        TENANT,
        @Schema(description = "业务分组")
        BUSINESS
    }
}
