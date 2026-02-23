package com.hkt.iot.workflow.infrastructure.persistence.mapper;

import com.hkt.iot.workflow.domain.model.entity.ProcessVariable;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.infrastructure.persistence.po.ProcessVariablePO;

import java.time.Duration;

/**
 * 流程变量映射器
 */
public final class ProcessVariableMapper {

    private ProcessVariableMapper() {}

    public static ProcessVariablePO toPO(ProcessVariable entity) {
        if (entity == null) return null;
        ProcessVariablePO po = new ProcessVariablePO();
        po.setId(entity.getId());
        po.setProcessInstanceId(entity.getProcessInstanceId().getValue());
        po.setName(entity.getName());
        po.setValue(entity.getValue());
        po.setType(entity.getType().name());
        po.setTenantId(entity.getTenantId().getValue());
        po.setCreatedAt(entity.getCreatedAt());
        po.setUpdatedAt(entity.getUpdatedAt());
        return po;
    }

    public static ProcessVariable toDomain(ProcessVariablePO po) {
        if (po == null) return null;
        ProcessVariable entity = new ProcessVariable();
        entity.setId(ProcessVariableId.of(po.getId()));
        entity.setProcessInstanceId(ProcessInstanceId.of(po.getProcessInstanceId()));
        entity.setName(po.getName());
        entity.setValue(po.getValue());
        entity.setType(ProcessVariableType.valueOf(po.getType()));
        entity.setTenantId(TenantId.of(po.getTenantId()));
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }
}
