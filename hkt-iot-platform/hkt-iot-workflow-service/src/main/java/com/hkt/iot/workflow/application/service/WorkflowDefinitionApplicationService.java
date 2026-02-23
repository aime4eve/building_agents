package com.hkt.iot.workflow.application.service;

import com.hkt.iot.workflow.application.command.ArchiveWorkflowDefinitionCommand;
import com.hkt.iot.workflow.application.command.CreateWorkflowDefinitionCommand;
import com.hkt.iot.workflow.application.command.PublishWorkflowDefinitionCommand;
import com.hkt.iot.workflow.application.dto.WorkflowDefinitionDTO;
import com.hkt.iot.workflow.application.query.WorkflowDefinitionQuery;
import com.hkt.iot.workflow.domain.model.aggregate.WorkflowDefinition;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.WorkflowDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 流程定义应用服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowDefinitionApplicationService {

    private final WorkflowDefinitionRepository workflowDefinitionRepository;

    @Transactional
    public WorkflowDefinitionDTO createDefinition(CreateWorkflowDefinitionCommand command) {
        WorkflowDefinitionKey key = WorkflowDefinitionKey.of(command.getDefinitionKey());
        
        if (workflowDefinitionRepository.existsByKey(key)) {
            throw new IllegalStateException("流程定义键已存在: " + command.getDefinitionKey());
        }

        WorkflowDefinition definition = WorkflowDefinition.create(
                key,
                command.getName(),
                command.getVersion(),
                command.getDescription(),
                TenantId.of(command.getTenantId()),
                UserId.of(command.getCreatedBy())
        );

        WorkflowDefinition saved = workflowDefinitionRepository.save(definition);
        log.info("Created workflow definition: id={}, key={}", saved.getId().getValue(), command.getDefinitionKey());

        return toDTO(saved);
    }

    @Transactional
    public WorkflowDefinitionDTO publishDefinition(PublishWorkflowDefinitionCommand command) {
        WorkflowDefinition definition = workflowDefinitionRepository
                .findById(WorkflowDefinitionId.of(command.getDefinitionId()))
                .orElseThrow(() -> new IllegalArgumentException("流程定义不存在: " + command.getDefinitionId()));

        definition.publish(UserId.of(command.getPublishedBy()));
        WorkflowDefinition saved = workflowDefinitionRepository.save(definition);
        log.info("Published workflow definition: id={}", command.getDefinitionId());

        return toDTO(saved);
    }

    @Transactional
    public WorkflowDefinitionDTO archiveDefinition(ArchiveWorkflowDefinitionCommand command) {
        WorkflowDefinition definition = workflowDefinitionRepository
                .findById(WorkflowDefinitionId.of(command.getDefinitionId()))
                .orElseThrow(() -> new IllegalArgumentException("流程定义不存在: " + command.getDefinitionId()));

        definition.archive(UserId.of(command.getArchivedBy()));
        WorkflowDefinition saved = workflowDefinitionRepository.save(definition);
        log.info("Archived workflow definition: id={}", command.getDefinitionId());

        return toDTO(saved);
    }

    public WorkflowDefinitionDTO getDefinition(String definitionId) {
        WorkflowDefinition definition = workflowDefinitionRepository
                .findById(WorkflowDefinitionId.of(definitionId))
                .orElseThrow(() -> new IllegalArgumentException("流程定义不存在: " + definitionId));
        return toDTO(definition);
    }

    public Optional<WorkflowDefinitionDTO> getLatestVersion(String definitionKey) {
        return workflowDefinitionRepository
                .findLatestByKey(WorkflowDefinitionKey.of(definitionKey))
                .map(this::toDTO);
    }

    public List<WorkflowDefinitionDTO> listDefinitions(WorkflowDefinitionQuery query) {
        List<WorkflowDefinition> definitions;
        
        if (query.getTenantId() != null && query.getStatus() != null) {
            definitions = workflowDefinitionRepository.findByTenantIdAndStatus(
                    TenantId.of(query.getTenantId()),
                    WorkflowDefinitionStatus.valueOf(query.getStatus()));
        } else if (query.getTenantId() != null) {
            definitions = workflowDefinitionRepository.findByTenantId(TenantId.of(query.getTenantId()));
        } else if (query.getStatus() != null) {
            definitions = workflowDefinitionRepository.findByStatus(
                    WorkflowDefinitionStatus.valueOf(query.getStatus()));
        } else {
            definitions = workflowDefinitionRepository.findByTenantId(TenantId.of(query.getTenantId()));
        }

        return definitions.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private WorkflowDefinitionDTO toDTO(WorkflowDefinition definition) {
        return WorkflowDefinitionDTO.builder()
                .definitionId(definition.getId().getValue())
                .definitionKey(definition.getKey().getValue())
                .name(definition.getName())
                .version(definition.getVersion())
                .status(definition.getStatus().name())
                .description(definition.getDescription())
                .tenantId(definition.getTenantId().getValue())
                .createdBy(definition.getCreatedBy().getValue())
                .createdAt(definition.getCreatedAt())
                .updatedAt(definition.getUpdatedAt())
                .publishedAt(definition.getPublishedAt())
                .build();
    }
}
