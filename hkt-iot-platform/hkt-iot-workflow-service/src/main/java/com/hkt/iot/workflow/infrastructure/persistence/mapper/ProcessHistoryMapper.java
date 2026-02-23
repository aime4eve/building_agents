package com.hkt.iot.workflow.infrastructure.persistence.mapper;

import com.hkt.iot.workflow.domain.model.entity.ProcessHistory;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.infrastructure.persistence.po.ProcessHistoryPO;

import java.time.Duration;

/**
 * 流程历史映射器
 */
public final class ProcessHistoryMapper {

    private ProcessHistoryMapper() {}

    public static ProcessHistoryPO toPO(ProcessHistory entity) {
        if (entity == null) return null;
        ProcessHistoryPO po = new ProcessHistoryPO();
        po.setId(entity.getId());
        po.setProcessInstanceId(entity.getProcessInstanceId().getValue());
        po.setTaskId(entity.getTaskId() != null ? entity.getTaskId().getValue() : null);
        po.setType(entity.getType().name());
        po.setActivityId(entity.getActivityId());
        po.setActivityName(entity.getActivityName());
        po.setFromState(entity.getFromState());
        po.setToState(entity.getToState());
        po.setOperatorId(entity.getOperatorId() != null ? entity.getOperatorId().getValue() : null);
        po.setOperatorName(entity.getOperatorName());
        po.setVariables(entity.getVariables());
        po.setDurationMillis(entity.getDuration() != null ? entity.getDuration().toMillis() : null);
        po.setTenantId(entity.getTenantId().getValue());
        po.setCreatedAt(entity.getCreatedAt());
        return po;
    }

    public static ProcessHistory toDomain(ProcessHistoryPO po) {
        if (po == null) return null;
        ProcessHistory entity = new ProcessHistory();
        entity.setId(ProcessHistoryId.of(po.getId()));
        entity.setProcessInstanceId(ProcessInstanceId.of(po.getProcessInstanceId()));
        entity.setTaskId(po.getTaskId() != null ? TaskId.of(po.getTaskId()) : null);
        entity.setType(ProcessHistoryType.valueOf(po.getType()));
        entity.setActivityId(po.getActivityId());
        entity.setActivityName(po.getActivityName());
        entity.setFromState(po.getFromState());
        entity.setToState(po.getToState());
        entity.setOperatorId(po.getOperatorId() != null ? UserId.of(po.getOperatorId()) : null);
        entity.setOperatorName(po.getOperatorName());
        entity.setVariables(po.getVariables());
        entity.setDuration(po.getDurationMillis() != null ? Duration.ofMillis(po.getDurationMillis()) : null);
        entity.setTenantId(TenantId.of(po.getTenantId()));
        entity.setCreatedAt(po.getCreatedAt());
        return entity;
    }
}
