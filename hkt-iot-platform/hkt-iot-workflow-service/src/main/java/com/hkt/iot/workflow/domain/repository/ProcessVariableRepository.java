package com.hkt.iot.workflow.domain.repository;

import com.hkt.iot.workflow.domain.model.entity.ProcessVariable;
import com.hkt.iot.workflow.domain.model.valueobject.ProcessInstanceId;
import com.hkt.iot.workflow.domain.model.valueobject.ProcessVariableId;

import java.util.List;
import java.util.Optional;

/**
 * 流程变量仓储接口
 */
public interface ProcessVariableRepository {

    ProcessVariable save(ProcessVariable processVariable);

    Optional<ProcessVariable> findById(ProcessVariableId id);

    List<ProcessVariable> findByProcessInstanceId(ProcessInstanceId processInstanceId);

    Optional<ProcessVariable> findByProcessInstanceIdAndName(ProcessInstanceId processInstanceId, String name);

    void delete(ProcessVariable processVariable);

    void deleteByProcessInstanceId(ProcessInstanceId processInstanceId);
}
