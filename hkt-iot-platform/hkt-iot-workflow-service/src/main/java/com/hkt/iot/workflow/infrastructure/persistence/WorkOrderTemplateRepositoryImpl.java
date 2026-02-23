package com.hkt.iot.workflow.infrastructure.persistence;

import com.hkt.iot.workflow.domain.model.entity.WorkOrderTemplate;
import com.hkt.iot.workflow.domain.model.valueobject.TenantId;
import com.hkt.iot.workflow.domain.model.valueobject.WorkOrderType;
import com.hkt.iot.workflow.domain.repository.WorkOrderTemplateRepository;
import com.hkt.iot.workflow.infrastructure.persistence.jpa.WorkOrderTemplateJpaRepository;
import com.hkt.iot.workflow.infrastructure.persistence.mapper.WorkOrderTemplateMapper;
import com.hkt.iot.workflow.infrastructure.persistence.po.WorkOrderTemplatePO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 工单模板仓储实现
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class WorkOrderTemplateRepositoryImpl implements WorkOrderTemplateRepository {

    private final WorkOrderTemplateJpaRepository jpaRepository;
    private final WorkOrderTemplateMapper mapper;

    @Override
    @Transactional
    public WorkOrderTemplate save(WorkOrderTemplate template) {
        WorkOrderTemplatePO po = mapper.toPO(template);
        WorkOrderTemplatePO savedPO = jpaRepository.save(po);
        log.debug("Saved work order template: id={}, name={}", savedPO.getId(), savedPO.getName());
        return mapper.toDomain(savedPO);
    }

    @Override
    public Optional<WorkOrderTemplate> findById(String id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<WorkOrderTemplate> findByTenantId(TenantId tenantId) {
        return jpaRepository.findByTenantId(tenantId.getValue()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrderTemplate> findByType(WorkOrderType type) {
        return jpaRepository.findByType(type.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WorkOrderTemplate> findByTenantIdAndType(TenantId tenantId, WorkOrderType type) {
        return jpaRepository.findByTenantIdAndType(tenantId.getValue(), type.name())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void delete(WorkOrderTemplate template) {
        jpaRepository.deleteById(template.getId());
        log.debug("Deleted work order template: id={}", template.getId());
    }
}
