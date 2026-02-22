package com.hkt.iot.space.interfaces.rest.dto;

import com.hkt.iot.space.domain.model.LogicalSpaceGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 逻辑空间分组响应DTO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "逻辑空间分组响应")
public class LogicalSpaceGroupResponse {

    @Schema(description = "分组ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "分组编码")
    private String groupCode;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "分组类型")
    private LogicalSpaceGroup.GroupType groupType;

    @Schema(description = "分组描述")
    private String description;

    @Schema(description = "分组颜色")
    private String groupColor;

    @Schema(description = "分组图标")
    private String groupIcon;

    @Schema(description = "分组规则")
    private Map<String, Object> groupRule;

    @Schema(description = "分组状态")
    private LogicalSpaceGroup.GroupStatus status;

    @Schema(description = "显示顺序")
    private Integer displayOrder;

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
     * @param group 逻辑空间分组领域模型
     * @return 逻辑空间分组响应DTO
     */
    public static LogicalSpaceGroupResponse from(LogicalSpaceGroup group) {
        return LogicalSpaceGroupResponse.builder()
                .id(group.getId())
                .tenantId(group.getTenantId())
                .groupCode(group.getGroupCode())
                .groupName(group.getGroupName())
                .groupType(group.getGroupType())
                .description(group.getDescription())
                .groupColor(group.getGroupColor())
                .groupIcon(group.getGroupIcon())
                .groupRule(group.getGroupRule())
                .status(group.getStatus())
                .displayOrder(group.getDisplayOrder())
                .version(group.getVersion())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .createdBy(group.getCreatedBy())
                .updatedBy(group.getUpdatedBy())
                .build();
    }
}
