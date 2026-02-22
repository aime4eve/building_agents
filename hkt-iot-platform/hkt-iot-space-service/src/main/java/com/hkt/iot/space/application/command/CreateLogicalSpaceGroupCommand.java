package com.hkt.iot.space.application.command;

import lombok.Value;

import java.util.Map;

/**
 * 创建逻辑空间分组命令对象
 *
 * @author HKT IoT Team
 */
@Value
public class CreateLogicalSpaceGroupCommand {

    /**
     * 租户ID
     */
    Long tenantId;

    /**
     * 分组编码
     */
    String groupCode;

    /**
     * 分组名称
     */
    String groupName;

    /**
     * 分组类型
     */
    GroupTypeCommand groupType;

    /**
     * 描述
     */
    String description;

    /**
     * 分组颜色
     */
    String groupColor;

    /**
     * 分组图标
     */
    String groupIcon;

    /**
     * 分组规则
     */
    Map<String, Object> groupRule;

    /**
     * 显示顺序
     */
    Integer displayOrder;

    /**
     * 创建人ID
     */
    Long createdBy;

    /**
     * 分组类型枚举
     */
    public enum GroupTypeCommand {
        APPLICATION, TENANT, BUSINESS
    }

    /**
     * 验证命令对象
     */
    public void validate() {
        if (tenantId == null) {
            throw new IllegalArgumentException("租户ID不能为空");
        }
        if (groupCode == null || groupCode.trim().isEmpty()) {
            throw new IllegalArgumentException("分组编码不能为空");
        }
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("分组名称不能为空");
        }
        if (groupType == null) {
            throw new IllegalArgumentException("分组类型不能为空");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("创建人ID不能为空");
        }
    }
}
