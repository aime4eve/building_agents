package com.hkt.iot.workflow.domain.repository;

import com.hkt.iot.workflow.domain.model.entity.AutoAssignRule;
import com.hkt.iot.workflow.domain.model.valueobject.TenantId;
import com.hkt.iot.workflow.domain.model.valueobject.WorkOrderType;

import java.util.List;
import java.util.Optional;

/**
 * 自动派单规则仓储接口
 *
 * @author HKT IoT Team
 */
public interface AutoAssignRuleRepository {

    AutoAssignRule save(AutoAssignRule rule);

    Optional<AutoAssignRule> findById(String id);

    List<AutoAssignRule> findByTenantId(TenantId tenantId);

    List<AutoAssignRule> findByWorkOrderType(WorkOrderType workOrderType);

    List<AutoAssignRule> findEnabledByTenantId(TenantId tenantId);

    List<AutoAssignRule> findEnabledByWorkOrderType(WorkOrderType workOrderType);

    void delete(AutoAssignRule rule);
}
