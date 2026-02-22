package com.hkt.iot.space.application.command;

import lombok.Value;

/**
 * 添加空间到分组命令对象
 *
 * @author HKT IoT Team
 */
@Value
public class AddSpaceToGroupCommand {

    /**
     * 租户ID
     */
    Long tenantId;

    /**
     * 分组ID
     */
    Long groupId;

    /**
     * 分组编码
     */
    String groupCode;

    /**
     * 空间ID
     */
    Long spaceId;

    /**
     * 空间编码
     */
    String spaceCode;

    /**
     * 空间名称
     */
    String spaceName;

    /**
     * 成员顺序
     */
    Integer memberOrder;

    /**
     * 是否置顶
     */
    Boolean isPinned;

    /**
     * 创建人ID
     */
    Long createdBy;

    /**
     * 验证命令对象
     */
    public void validate() {
        if (tenantId == null) {
            throw new IllegalArgumentException("租户ID不能为空");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("分组ID不能为空");
        }
        if (groupCode == null || groupCode.trim().isEmpty()) {
            throw new IllegalArgumentException("分组编码不能为空");
        }
        if (spaceId == null) {
            throw new IllegalArgumentException("空间ID不能为空");
        }
        if (spaceCode == null || spaceCode.trim().isEmpty()) {
            throw new IllegalArgumentException("空间编码不能为空");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("创建人ID不能为空");
        }
    }
}
