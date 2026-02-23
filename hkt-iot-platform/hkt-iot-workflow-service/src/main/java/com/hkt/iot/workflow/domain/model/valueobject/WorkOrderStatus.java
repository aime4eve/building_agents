package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.Getter;

/**
 * 工单状态枚举
 *
 * @author HKT IoT Team
 */
@Getter
public enum WorkOrderStatus {
    CREATED("已创建", true),
    PENDING_ASSIGN("待分配", true),
    ASSIGNED("已分配", true),
    PROCESSING("处理中", true),
    PENDING_CONFIRM("待确认", true),
    COMPLETED("已完成", false),
    CANCELLED("已取消", false),
    REJECTED("已驳回", false);

    private final String description;
    private final boolean canTransition;

    WorkOrderStatus(String description, boolean canTransition) {
        this.description = description;
        this.canTransition = canTransition;
    }

    public boolean canTransitionTo(WorkOrderStatus target) {
        if (!this.canTransition) {
            return false;
        }
        return switch (this) {
            case CREATED -> target == PENDING_ASSIGN || target == CANCELLED;
            case PENDING_ASSIGN -> target == ASSIGNED || target == CANCELLED;
            case ASSIGNED -> target == PROCESSING || target == PENDING_ASSIGN || target == CANCELLED;
            case PROCESSING -> target == PENDING_CONFIRM || target == ASSIGNED || target == CANCELLED;
            case PENDING_CONFIRM -> target == COMPLETED || target == PROCESSING || target == REJECTED;
            default -> false;
        };
    }
}
