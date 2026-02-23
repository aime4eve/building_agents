package com.hkt.iot.workflow.infrastructure.persistence;

import com.hkt.iot.workflow.domain.model.entity.AutoAssignRule;
import com.hkt.iot.workflow.domain.model.valueobject.TenantId;
import com.hkt.iot.workflow.domain.model.valueobject.WorkOrderType;
import com.hkt.iot.workflow.domain.repository.AutoAssignRuleRepository;
import com.hkt.iot.workflow.infrastructure.persistence.jpa.AutoAssignRuleJpaRepository;
import com.hkt.iot.workflow.infrastructure.persistence.mapper.AutoAssignRuleMapper;
import com.hkt.iot.workflow.infrastructure.persistence.po.AutoAssignRulePO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 自动派单规则仓储实现
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class AutoAssignRuleRepositoryImpl implements AutoAssignRuleRepository {

    private final AutoAssignRuleJpaRepository jpaRepository;
    private final AutoAssignRuleMapper mapper;

    @Override
    @Transactional
    public AutoAssignRule save(AutoAssignRule rule) {
        AutoAssignRulePO po = mapper.toPO(rule);
        AutoAssignRulePO savedPO = jpaRepository.save(po);
        log.debug("Saved auto assign rule: id={}, name={}", savedPO.getId(), savedPO.getName());
        return mapper.toDomain(savedPO);
    }

    @Override
    public Optional<AutoAssignRule> findById(String id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<AutoAssignRule> findByTenantId(TenantId tenantId) {
        return jpaRepository.findByTenantId(tenantId.getValue()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AutoAssignRule> findByWorkOrderType(WorkOrderType workOrderType) {
        return jpaRepository.findByWorkOrderType(workOrderType.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AutoAssignRule> findEnabledByTenantId(TenantId tenantId) {
        return jpaRepository.findByTenantIdAndEnabled(tenantId.getValue(), true).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AutoAssignRule> findEnabledByWorkOrderType(WorkOrderType workOrderType) {
        return jpaRepository.findByWorkOrderTypeAndEnabled(workOrderType.name(), true).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(AutoAssignRule rule) {
        jpaRepository.deleteById(rule.getId());
        log.debug("Deleted auto assign rule: id={}", rule.getId());
    }
}
