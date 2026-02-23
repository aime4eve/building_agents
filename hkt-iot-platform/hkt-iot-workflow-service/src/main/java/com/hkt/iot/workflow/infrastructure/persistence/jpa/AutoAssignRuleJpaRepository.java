package com.hkt.iot.workflow.infrastructure.persistence.jpa;

import com.hkt.iot.workflow.infrastructure.persistence.po.AutoAssignRulePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 自动派单规则 JPA Repository
 *
 * @author HKT IoT Team
 */
@Repository
public interface AutoAssignRuleJpaRepository extends JpaRepository<AutoAssignRulePO, String> {

    List<AutoAssignRulePO> findByTenantId(String tenantId);

    List<AutoAssignRulePO> findByWorkOrderType(String workOrderType);

    List<AutoAssignRulePO> findByTenantIdAndEnabled(String tenantId, Boolean enabled);

    List<AutoAssignRulePO> findByWorkOrderTypeAndEnabled(String workOrderType, Boolean enabled);

    Optional<AutoAssignRulePO> findByIdAndEnabled(String id, Boolean enabled);
}
