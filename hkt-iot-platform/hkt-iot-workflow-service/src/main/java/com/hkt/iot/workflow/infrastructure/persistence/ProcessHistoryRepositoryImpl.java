package com.hkt.iot.workflow.infrastructure.persistence;

import com.hkt.iot.workflow.domain.model.entity.ProcessHistory;
import com.hkt.iot.workflow.domain.model.valueobject.ProcessHistoryId;
import com.hkt.iot.workflow.domain.model.valueobject.ProcessHistoryType;
import com.hkt.iot.workflow.domain.model.valueobject.ProcessInstanceId;
import com.hkt.iot.workflow.domain.model.valueobject.TaskId;
import com.hkt.iot.workflow.domain.repository.ProcessHistoryRepository;
import com.hkt.iot.workflow.infrastructure.persistence.jpa.ProcessHistoryJpaRepository;
import com.hkt.iot.workflow.infrastructure.persistence.mapper.ProcessHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 流程历史仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ProcessHistoryRepositoryImpl implements ProcessHistoryRepository {

    private final ProcessHistoryJpaRepository jpaRepository;

    @Override
    public ProcessHistory save(ProcessHistory processHistory) {
        var po = ProcessHistoryMapper.toPO(processHistory);
        var saved = jpaRepository.save(po);
        return ProcessHistoryMapper.toDomain(saved);
    }

    @Override
    public Optional<ProcessHistory> findById(ProcessHistoryId id) {
        return jpaRepository.findById(id.getValue())
                .map(ProcessHistoryMapper::toDomain);
    }

    @Override
    public List<ProcessHistory> findByProcessInstanceId(ProcessInstanceId processInstanceId) {
        return jpaRepository.findByProcessInstanceId(processInstanceId.getValue())
                .stream()
                .map(ProcessHistoryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessHistory> findByProcessInstanceIdAndType(ProcessInstanceId processInstanceId, ProcessHistoryType type) {
        return jpaRepository.findByProcessInstanceIdAndType(processInstanceId.getValue(), type.name())
                .stream()
                .map(ProcessHistoryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessHistory> findByTaskId(TaskId taskId) {
        return jpaRepository.findByTaskId(taskId.getValue())
                .stream()
                .map(ProcessHistoryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(ProcessHistory processHistory) {
        jpaRepository.deleteById(processHistory.getId());
    }
}
