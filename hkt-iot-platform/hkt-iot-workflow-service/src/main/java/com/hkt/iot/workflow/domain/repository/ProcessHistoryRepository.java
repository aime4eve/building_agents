package com.hkt.iot.workflow.domain.repository;

import com.hkt.iot.workflow.domain.model.entity.ProcessHistory;
import com.hkt.iot.workflow.domain.model.valueobject.ProcessHistoryId;
import com.hkt.iot.workflow.domain.model.valueobject.ProcessHistoryType;
import com.hkt.iot.workflow.domain.model.valueobject.ProcessInstanceId;
import com.hkt.iot.workflow.domain.model.valueobject.TaskId;

import java.util.List;
import java.util.Optional;

/**
 * 流程历史仓储接口
 */
public interface ProcessHistoryRepository {

    ProcessHistory save(ProcessHistory processHistory);

    Optional<ProcessHistory> findById(ProcessHistoryId id);

    List<ProcessHistory> findByProcessInstanceId(ProcessInstanceId processInstanceId);

    List<ProcessHistory> findByProcessInstanceIdAndType(ProcessInstanceId processInstanceId, ProcessHistoryType type);

    List<ProcessHistory> findByTaskId(TaskId taskId);

    void delete(ProcessHistory processHistory);
}
