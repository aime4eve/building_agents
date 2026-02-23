package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.Getter;

/**
 * 工单类型枚举
 *
 * @author HKT IoT Team
 */
@Getter
public enum WorkOrderType {
    REPAIR("REP", "维修工单"),
    MAINTAIN("MNT", "维护工单"),
    INSPECT("INS", "巡检工单"),
    COMPLAIN("CMP", "投诉工单"),
    CONSULT("CON", "咨询工单");

    private final String prefix;
    private final String description;

    WorkOrderType(String prefix, String description) {
        this.prefix = prefix;
        this.description = description;
    }
}
