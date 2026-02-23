package com.hkt.iot.workflow.infrastructure.persistence;

import com.hkt.iot.workflow.domain.model.entity.ProcessVariable;
import com.hkt.iot.workflow.domain.model.valueobject.ProcessInstanceId;
import com.hkt.iot.workflow.domain.model.valueobject.ProcessVariableId;
import com.hkt.iot.workflow.domain.repository.ProcessVariableRepository;
import com.hkt.iot.workflow.infrastructure.persistence.jpa.ProcessVariableJpaRepository;
import com.hkt.iot.workflow.infrastructure.persistence.mapper.ProcessVariableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 流程变量仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ProcessVariableRepositoryImpl implements ProcessVariableRepository {

    private final ProcessVariableJpaRepository jpaRepository;

    @Override
    public ProcessVariable save(ProcessVariable processVariable) {
        var po = ProcessVariableMapper.toPO(processVariable);
        var saved = jpaRepository.save(po);
        return ProcessVariableMapper.toDomain(saved);
    }

    @Override
    public Optional<ProcessVariable> findById(ProcessVariableId id) {
        return jpaRepository.findById(id.getValue())
                .map(ProcessVariableMapper::toDomain);
    }

    @Override
    public List<ProcessVariable> findByProcessInstanceId(ProcessInstanceId processInstanceId) {
        return jpaRepository.findByProcessInstanceId(processInstanceId.getValue())
                .stream()
                .map(ProcessVariableMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProcessVariable> findByProcessInstanceIdAndName(ProcessInstanceId processInstanceId, String name) {
        return jpaRepository.findByProcessInstanceIdAndName(processInstanceId.getValue(), name)
                .map(ProcessVariableMapper::toDomain);
    }

    @Override
    public void delete(ProcessVariable processVariable) {
        jpaRepository.deleteById(processVariable.getId());
    }

    @Override
    public void deleteByProcessInstanceId(ProcessInstanceId processInstanceId) {
        jpaRepository.deleteByProcessInstanceId(processInstanceId.getValue());
    }
}
