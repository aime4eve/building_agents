package com.hkt.iot.workflow.infrastructure.persistence;

import com.hkt.iot.workflow.domain.model.aggregate.WorkflowDefinition;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.WorkflowDefinitionRepository;
import com.hkt.iot.workflow.infrastructure.persistence.jpa.WorkflowDefinitionJpaRepository;
import com.hkt.iot.workflow.infrastructure.persistence.mapper.WorkflowDefinitionMapper;
import com.hkt.iot.workflow.infrastructure.persistence.po.WorkflowDefinitionPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 流程定义仓储实现
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class WorkflowDefinitionRepositoryImpl implements WorkflowDefinitionRepository {

    private final WorkflowDefinitionJpaRepository jpaRepository;
    private final WorkflowDefinitionMapper mapper;

    @Override
    @Transactional
    public WorkflowDefinition save(WorkflowDefinition workflowDefinition) {
        WorkflowDefinitionPO po = mapper.toPO(workflowDefinition);
        WorkflowDefinitionPO savedPO = jpaRepository.save(po);
        log.debug("Saved workflow definition: id={}, key={}", savedPO.getId(), savedPO.getDefinitionKey());
        return mapper.toDomain(savedPO);
    }

    @Override
    public Optional<WorkflowDefinition> findById(WorkflowDefinitionId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<WorkflowDefinition> findByKey(WorkflowDefinitionKey key) {
        return jpaRepository.findByDefinitionKey(key.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public List<WorkflowDefinition> findByTenantId(TenantId tenantId) {
        return jpaRepository.findByTenantId(tenantId.getValue()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowDefinition> findByStatus(WorkflowDefinitionStatus status) {
        return jpaRepository.findByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowDefinition> findByTenantIdAndStatus(TenantId tenantId, WorkflowDefinitionStatus status) {
        return jpaRepository.findByTenantIdAndStatus(tenantId.getValue(), status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WorkflowDefinition> findByKeyAndVersion(WorkflowDefinitionKey key, String version) {
        return jpaRepository.findByDefinitionKeyAndVersion(key.getValue(), version)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void delete(WorkflowDefinition workflowDefinition) {
        WorkflowDefinitionPO po = mapper.toPO(workflowDefinition);
        jpaRepository.delete(po);
        log.debug("Deleted workflow definition: id={}", workflowDefinition.getId());
    }

    @Override
    public boolean existsByKey(WorkflowDefinitionKey key) {
        return jpaRepository.existsByDefinitionKey(key.getValue());
    }
}
