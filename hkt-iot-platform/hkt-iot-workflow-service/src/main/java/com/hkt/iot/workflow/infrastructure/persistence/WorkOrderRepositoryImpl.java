package com.hkt.iot.workflow.infrastructure.persistence;

import com.hkt.iot.workflow.domain.model.aggregate.WorkOrder;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.WorkOrderRepository;
import com.hkt.iot.workflow.infrastructure.persistence.jpa.WorkOrderJpaRepository;
import com.hkt.iot.workflow.infrastructure.persistence.mapper.WorkOrderMapper;
import com.hkt.iot.workflow.infrastructure.persistence.po.WorkOrderPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 工单仓储实现
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class WorkOrderRepositoryImpl implements WorkOrderRepository {

    private final WorkOrderJpaRepository jpaRepository;
    private final WorkOrderMapper mapper;

    @Override
    @Transactional
    public WorkOrder save(WorkOrder workOrder) {
        WorkOrderPO po = mapper.toPO(workOrder);
        WorkOrderPO savedPO = jpaRepository.save(po);
        log.debug("Saved work order: id={}, workOrderNo={}", savedPO.getId(), savedPO.getWorkOrderNo());
        return mapper.toDomain(savedPO);
    }

    @Override
    public Optional<WorkOrder> findById(WorkOrderId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<WorkOrder> findByWorkOrderNo(WorkOrderNo workOrderNo) {
        return jpaRepository.findByWorkOrderNo(workOrderNo.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public List<WorkOrder> findByTenantId(TenantId tenantId) {
        return jpaRepository.findByTenantId(tenantId.getValue()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> findByStatus(WorkOrderStatus status) {
        return jpaRepository.findByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> findByAssigneeId(UserId assigneeId) {
        return jpaRepository.findByAssigneeId(assigneeId.getValue()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> findByReporterId(UserId reporterId) {
        return jpaRepository.findByReporterId(reporterId.getValue()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> findOverdue() {
        return jpaRepository.findOverdue(LocalDateTime.now()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> findByTenantIdAndStatus(TenantId tenantId, WorkOrderStatus status) {
        return jpaRepository.findByTenantIdAndStatus(tenantId.getValue(), status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkOrder> findBySpaceId(String spaceId) {
        return jpaRepository.findBySpaceId(spaceId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(WorkOrder workOrder) {
        jpaRepository.deleteById(workOrder.getId());
        log.debug("Deleted work order: id={}", workOrder.getId());
    }
}
