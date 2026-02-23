package com.hkt.iot.workflow.infrastructure.persistence;

import com.hkt.iot.workflow.domain.model.entity.WorkflowTask;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.WorkflowTaskRepository;
import com.hkt.iot.workflow.infrastructure.persistence.jpa.WorkflowTaskJpaRepository;
import com.hkt.iot.workflow.infrastructure.persistence.mapper.WorkflowTaskMapper;
import com.hkt.iot.workflow.infrastructure.persistence.po.WorkflowTaskPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 工作流任务仓储实现
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class WorkflowTaskRepositoryImpl implements WorkflowTaskRepository {

    private final WorkflowTaskJpaRepository jpaRepository;
    private final WorkflowTaskMapper mapper;

    @Override
    @Transactional
    public WorkflowTask save(WorkflowTask task) {
        WorkflowTaskPO po = mapper.toPO(task);
        WorkflowTaskPO savedPO = jpaRepository.save(po);
        log.debug("Saved workflow task: id={}, taskName={}", savedPO.getId(), savedPO.getTaskName());
        return mapper.toDomain(savedPO);
    }

    @Override
    public Optional<WorkflowTask> findById(TaskId id) {
        return jpaRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public List<WorkflowTask> findByProcessInstanceId(ProcessInstanceId processInstanceId) {
        return jpaRepository.findByProcessInstanceId(processInstanceId.getValue()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowTask> findByAssignee(UserId assignee) {
        return jpaRepository.findByAssignee(assignee.getValue()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowTask> findByTenantIdAndState(TenantId tenantId, TaskStatus status) {
        return jpaRepository.findByTenantIdAndState(tenantId.getValue(), status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(WorkflowTask task) {
        WorkflowTaskPO po = mapper.toPO(task);
        jpaRepository.delete(po);
        log.debug("Deleted workflow task: id={}", task.getId());
    }
}
