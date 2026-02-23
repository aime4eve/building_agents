package com.hkt.iot.workflow.application.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程定义查询对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDefinitionQuery {

    private String tenantId;
    private String definitionKey;
    private String status;
    private Integer page;
    private Integer size;
}
