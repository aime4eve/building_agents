package com.hkt.iot.workflow.domain.model.valueobject;

/**
 * 流程历史类型枚举
 */
public enum ProcessHistoryType {
    PROCESS_START,
    PROCESS_END,
    TASK_CREATE,
    TASK_COMPLETE,
    TASK_ASSIGN,
    VARIABLE_UPDATE,
    STATE_CHANGE
}
