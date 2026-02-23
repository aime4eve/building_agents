package com.hkt.iot.workflow.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 归档流程定义命令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveWorkflowDefinitionCommand {

    private String definitionId;
    private String tenantId;
    private String archivedBy;
}
