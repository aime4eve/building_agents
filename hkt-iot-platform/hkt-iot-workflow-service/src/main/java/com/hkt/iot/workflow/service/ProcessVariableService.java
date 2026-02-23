package com.hkt.iot.workflow.service;

import com.hkt.iot.workflow.domain.model.entity.ProcessVariable;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.ProcessVariableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 流程变量服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessVariableService {

    private final ProcessVariableRepository processVariableRepository;

    @Transactional
    public ProcessVariable setVariable(
            ProcessInstanceId processInstanceId,
            String name,
            String value,
            ProcessVariableType type,
            TenantId tenantId) {
        Optional<ProcessVariable> existing = processVariableRepository
                .findByProcessInstanceIdAndName(processInstanceId, name);
        
        if (existing.isPresent()) {
            ProcessVariable variable = existing.get();
            variable.updateValue(value);
            return processVariableRepository.save(variable);
        } else {
            ProcessVariable variable = ProcessVariable.create(
                    processInstanceId, name, value, type, tenantId);
            return processVariableRepository.save(variable);
        }
    }

    public Optional<ProcessVariable> getVariable(ProcessInstanceId processInstanceId, String name) {
        return processVariableRepository.findByProcessInstanceIdAndName(processInstanceId, name);
    }

    public List<ProcessVariable> getVariables(ProcessInstanceId processInstanceId) {
        return processVariableRepository.findByProcessInstanceId(processInstanceId);
    }

    @Transactional
    public void deleteVariables(ProcessInstanceId processInstanceId) {
        processVariableRepository.deleteByProcessInstanceId(processInstanceId);
        log.info("Deleted all variables for process instance: {}", processInstanceId.getValue());
    }

    @Transactional
    public void setVariables(
            ProcessInstanceId processInstanceId,
            Map<String, String> variables,
            ProcessVariableType defaultType,
            TenantId tenantId) {
        variables.forEach((name, value) -> 
                setVariable(processInstanceId, name, value, defaultType, tenantId));
    }
}
