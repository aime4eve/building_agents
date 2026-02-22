package com.hkt.iot.space.application.command;

import lombok.Value;

/**
 * 从分组移除空间命令对象
 *
 * @author HKT IoT Team
 */
@Value
public class RemoveSpaceFromGroupCommand {

    /**
     * 分组成员ID
     */
    Long groupMemberId;

    /**
     * 删除人ID
     */
    Long deletedBy;

    /**
     * 验证命令对象
     */
    public void validate() {
        if (groupMemberId == null) {
            throw new IllegalArgumentException("分组成员ID不能为空");
        }
        if (deletedBy == null) {
            throw new IllegalArgumentException("删除人ID不能为空");
        }
    }
}
