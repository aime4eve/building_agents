package com.hkt.iot.space.application.command;

import lombok.Value;

/**
 * 解绑资源命令对象
 *
 * @author HKT IoT Team
 */
@Value
public class UnbindResourceCommand {

    /**
     * 空间资源关联ID
     */
    Long spaceResourceId;

    /**
     * 删除人ID
     */
    Long deletedBy;

    /**
     * 验证命令对象
     */
    public void validate() {
        if (spaceResourceId == null) {
            throw new IllegalArgumentException("空间资源关联ID不能为空");
        }
        if (deletedBy == null) {
            throw new IllegalArgumentException("删除人ID不能为空");
        }
    }
}
