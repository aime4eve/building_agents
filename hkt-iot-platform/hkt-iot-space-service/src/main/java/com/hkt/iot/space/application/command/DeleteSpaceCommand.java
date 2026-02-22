package com.hkt.iot.space.application.command;

import lombok.Value;

/**
 * 删除空间命令对象
 *
 * @author HKT IoT Team
 */
@Value
public class DeleteSpaceCommand {

    /**
     * 空间ID
     */
    Long spaceId;

    /**
     * 删除人ID
     */
    Long deletedBy;

    /**
     * 版本号（乐观锁）
     */
    Long version;

    /**
     * 验证命令对象
     */
    public void validate() {
        if (spaceId == null) {
            throw new IllegalArgumentException("空间ID不能为空");
        }
        if (deletedBy == null) {
            throw new IllegalArgumentException("删除人ID不能为空");
        }
        if (version == null) {
            throw new IllegalArgumentException("版本号不能为空");
        }
    }
}
