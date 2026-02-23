package com.hkt.iot.workflow.domain.repository;

import com.hkt.iot.workflow.domain.model.entity.WorkOrderTemplate;
import com.hkt.iot.workflow.domain.model.valueobject.TenantId;
import com.hkt.iot.workflow.domain.model.valueobject.WorkOrderType;

import java.util.List;
import java.util.Optional;

/**
 * 工单模板仓储接口
 *
 * @author HKT IoT Team
 */
public interface WorkOrderTemplateRepository {

    WorkOrderTemplate save(WorkOrderTemplate template);

    Optional<WorkOrderTemplate> findById(String id);

    List<WorkOrderTemplate> findByTenantId(TenantId tenantId);

    List<WorkOrderTemplate> findByType(WorkOrderType type);

    Optional<WorkOrderTemplate> findByTenantIdAndType(TenantId tenantId, WorkOrderType type);

    void delete(WorkOrderTemplate template);
}
