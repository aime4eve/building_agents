package com.hkt.iot.workflow.infrastructure.persistence;

import com.hkt.iot.workflow.domain.model.entity.SLAConfig;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.SLAConfigRepository;
import com.hkt.iot.workflow.infrastructure.persistence.jpa.SLAConfigJpaRepository;
import com.hkt.iot.workflow.infrastructure.persistence.mapper.SLAConfigMapper;
import com.hkt.iot.workflow.infrastructure.persistence.po.SLAConfigPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SLA 配置仓储实现
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class SLAConfigRepositoryImpl implements SLAConfigRepository {

    private final SLAConfigJpaRepository jpaRepository;
    private final SLAConfigMapper mapper;

    @Override
    @Transactional
    public SLAConfig save(SLAConfig config) {
        SLAConfigPO po = mapper.toPO(config);
        SLAConfigPO savedPO = jpaRepository.save(po);
        log.debug("Saved SLA config: id={}, processDefinitionKey={}", savedPO.getId(), savedPO.getProcessDefinitionKey());
        return mapper.toDomain(savedPO);
    }

    @Override
    public Optional<SLAConfig> findById(String id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<SLAConfig> findByProcessDefinitionKey(ProcessDefinitionKey processDefinitionKey) {
        return jpaRepository.findByProcessDefinitionKey(processDefinitionKey.getValue()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SLAConfig> findByProcessDefinitionKeyAndTaskDefinitionKey(
            ProcessDefinitionKey processDefinitionKey,
            ActivityId taskDefinitionKey) {
        return jpaRepository.findByProcessDefinitionKeyAndTaskDefinitionKey(
                        processDefinitionKey.getValue(),
                        taskDefinitionKey != null ? taskDefinitionKey.getValue() : null)
                .map(mapper::toDomain);
    }

    @Override
    public List<SLAConfig> findByTenantId(TenantId tenantId) {
        return jpaRepository.findByTenantId(tenantId.getValue()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(SLAConfig config) {
        SLAConfigPO po = mapper.toPO(config);
        jpaRepository.delete(po);
        log.debug("Deleted SLA config: id={}", config.getId());
    }
}
