package com.hkt.iot.workflow.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 创建流程定义命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkflowDefinitionCommand {

    private String tenantId;
    private String definitionKey;
    private String name;
    private String version;
    private String description;
    private String createdBy;
}
