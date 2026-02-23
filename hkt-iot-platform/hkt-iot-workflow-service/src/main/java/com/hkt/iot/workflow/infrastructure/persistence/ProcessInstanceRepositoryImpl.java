package com.hkt.iot.workflow.infrastructure.persistence;

import com.hkt.iot.workflow.domain.model.aggregate.ProcessInstance;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.ProcessInstanceRepository;
import com.hkt.iot.workflow.infrastructure.persistence.jpa.ProcessInstanceJpaRepository;
import com.hkt.iot.workflow.infrastructure.persistence.mapper.ProcessInstanceMapper;
import com.hkt.iot.workflow.infrastructure.persistence.po.ProcessInstancePO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 流程实例仓储实现
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class ProcessInstanceRepositoryImpl implements ProcessInstanceRepository {

    private final ProcessInstanceJpaRepository jpaRepository;
    private final ProcessInstanceMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public ProcessInstance save(ProcessInstance processInstance) {
        ProcessInstancePO po = mapper.toPO(processInstance);
        ProcessInstancePO savedPO = jpaRepository.save(po);
        log.debug("Saved process instance: id={}, businessKey={}", savedPO.getId(), savedPO.getBusinessKey());
        return mapper.toDomain(savedPO);
    }

    @Override
    public Optional<ProcessInstance> findById(ProcessInstanceId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<ProcessInstance> findByBusinessKey(BusinessKey businessKey) {
        return jpaRepository.findByBusinessKey(businessKey.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public List<ProcessInstance> findByTenantId(TenantId tenantId) {
        return jpaRepository.findByTenantId(tenantId.getValue()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessInstance> findByState(ProcessInstanceState state) {
        return jpaRepository.findByState(state.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessInstance> findByTenantIdAndState(TenantId tenantId, ProcessInstanceState state) {
        return jpaRepository.findByTenantIdAndState(tenantId.getValue(), state.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(ProcessInstance processInstance) {
        ProcessInstancePO po = mapper.toPO(processInstance);
        jpaRepository.delete(po);
        log.debug("Deleted process instance: id={}", processInstance.getId());
    }

    @Override
    public boolean existsByBusinessKey(BusinessKey businessKey) {
        return jpaRepository.existsByBusinessKey(businessKey.getValue());
    }
}
