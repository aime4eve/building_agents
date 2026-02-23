package com.hkt.iot.workflow.infrastructure.persistence;

import com.hkt.iot.workflow.domain.model.entity.SLAMonitor;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.SLAMonitorRepository;
import com.hkt.iot.workflow.infrastructure.persistence.jpa.SLAMonitorJpaRepository;
import com.hkt.iot.workflow.infrastructure.persistence.mapper.SLAMonitorMapper;
import com.hkt.iot.workflow.infrastructure.persistence.po.SLAMonitorPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SLA 监控记录仓储实现
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class SLAMonitorRepositoryImpl implements SLAMonitorRepository {

    private final SLAMonitorJpaRepository jpaRepository;
    private final SLAMonitorMapper mapper;

    @Override
    @Transactional
    public SLAMonitor save(SLAMonitor monitor) {
        SLAMonitorPO po = mapper.toPO(monitor);
        SLAMonitorPO savedPO = jpaRepository.save(po);
        log.debug("Saved SLA monitor: id={}, processInstanceId={}", savedPO.getId(), savedPO.getProcessInstanceId());
        return mapper.toDomain(savedPO);
    }

    @Override
    public Optional<SLAMonitor> findById(SLAMonitorId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<SLAMonitor> findByProcessInstanceId(ProcessInstanceId processInstanceId) {
        return jpaRepository.findByProcessInstanceId(processInstanceId.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<SLAMonitor> findByTaskId(TaskId taskId) {
        return jpaRepository.findByTaskId(taskId.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public List<SLAMonitor> findByTenantIdAndCreatedAtBetween(
            TenantId tenantId,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        return jpaRepository.findByTenantIdAndCreatedAtBetween(tenantId.getValue(), startTime, endTime).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<SLAMonitor> findByTenantIdAndResponseStatus(TenantId tenantId, SLAStatus status) {
        return jpaRepository.findByTenantIdAndResponseStatus(tenantId.getValue(), status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(SLAMonitor monitor) {
        SLAMonitorPO po = mapper.toPO(monitor);
        jpaRepository.delete(po);
        log.debug("Deleted SLA monitor: id={}", monitor.getId());
    }
}
