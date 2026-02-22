package com.hkt.iot.space.application.dto;

import com.hkt.iot.space.domain.model.LogicalSpaceGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 逻辑空间分组DTO
 * 用于逻辑空间分组数据的传输
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogicalSpaceGroupDTO {

    /**
     * 分组ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 分组编码
     */
    private String groupCode;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 分组类型
     */
    private GroupTypeDTO groupType;

    /**
     * 描述
     */
    private String description;

    /**
     * 分组颜色
     */
    private String groupColor;

    /**
     * 分组图标
     */
    private String groupIcon;

    /**
     * 分组规则
     */
    private Map<String, Object> groupRule;

    /**
     * 状态
     */
    private GroupStatusDTO status;

    /**
     * 显示顺序
     */
    private Integer displayOrder;

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
     * 分组成员数量
     */
    private Integer memberCount;

    /**
     * 分组类型枚举
     */
    public enum GroupTypeDTO {
        APPLICATION, TENANT, BUSINESS
    }

    /**
     * 分组状态枚举
     */
    public enum GroupStatusDTO {
        ACTIVE, INACTIVE
    }

    /**
     * 从领域模型转换为DTO
     *
     * @param group 逻辑空间分组领域模型
     * @param memberCount 分组成员数量
     * @return 逻辑空间分组DTO
     */
    public static LogicalSpaceGroupDTO from(LogicalSpaceGroup group, Integer memberCount) {
        return LogicalSpaceGroupDTO.builder()
                .id(group.getId())
                .tenantId(group.getTenantId())
                .groupCode(group.getGroupCode())
                .groupName(group.getGroupName())
                .groupType(convertGroupType(group.getGroupType()))
                .description(group.getDescription())
                .groupColor(group.getGroupColor())
                .groupIcon(group.getGroupIcon())
                .groupRule(group.getGroupRule())
                .status(convertGroupStatus(group.getStatus()))
                .displayOrder(group.getDisplayOrder())
                .version(group.getVersion())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .createdBy(group.getCreatedBy())
                .updatedBy(group.getUpdatedBy())
                .memberCount(memberCount)
                .build();
    }

    /**
     * 从领域模型转换为DTO（不包含成员数量）
     *
     * @param group 逻辑空间分组领域模型
     * @return 逻辑空间分组DTO
     */
    public static LogicalSpaceGroupDTO from(LogicalSpaceGroup group) {
        return from(group, null);
    }

    /**
     * 转换分组类型枚举
     */
    private static GroupTypeDTO convertGroupType(LogicalSpaceGroup.GroupType groupType) {
        if (groupType == null) {
            return null;
        }
        return GroupTypeDTO.valueOf(groupType.name());
    }

    /**
     * 转换分组状态枚举
     */
    private static GroupStatusDTO convertGroupStatus(LogicalSpaceGroup.GroupStatus groupStatus) {
        if (groupStatus == null) {
            return null;
        }
        return GroupStatusDTO.valueOf(groupStatus.name());
    }
}
