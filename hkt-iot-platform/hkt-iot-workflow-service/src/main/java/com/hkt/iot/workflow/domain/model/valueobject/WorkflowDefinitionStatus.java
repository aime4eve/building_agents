package com.hkt.iot.workflow.domain.model.valueobject;

/**
 * 流程定义状态枚举
 *
 * @author HKT IoT Team
 */
public enum WorkflowDefinitionStatus {
    /**
     * 草稿状态 - 流程定义正在编辑中
     */
    DRAFT,
    /**
     * 已发布状态 - 流程定义已发布可用
     */
    PUBLISHED,
    /**
     * 已归档状态 - 流程定义已归档不可用
     */
    ARCHIVED
}
