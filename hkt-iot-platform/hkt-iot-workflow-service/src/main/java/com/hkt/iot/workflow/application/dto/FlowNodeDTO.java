package com.hkt.iot.workflow.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流程节点DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowNodeDTO {

    private String nodeId;
    private String nodeKey;
    private String nodeName;
    private String nodeType;
    private String workflowDefinitionId;
    private Integer order;
    private String config;
    private String tenantId;
}
