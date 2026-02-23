package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.Getter;

/**
 * 工单优先级枚举
 *
 * @author HKT IoT Team
 */
@Getter
public enum WorkOrderPriority {
    LOW(1, "低优先级", 72),
    NORMAL(2, "普通优先级", 48),
    HIGH(3, "高优先级", 24),
    URGENT(4, "紧急优先级", 4);

    private final int level;
    private final String description;
    private final int slaHours;

    WorkOrderPriority(int level, String description, int slaHours) {
        this.level = level;
        this.description = description;
        this.slaHours = slaHours;
    }

    public static WorkOrderPriority fromLevel(int level) {
        for (WorkOrderPriority priority : values()) {
            if (priority.level == level) {
                return priority;
            }
        }
        return NORMAL;
    }
}
