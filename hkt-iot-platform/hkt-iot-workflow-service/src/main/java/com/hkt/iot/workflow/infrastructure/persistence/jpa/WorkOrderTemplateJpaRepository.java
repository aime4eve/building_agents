package com.hkt.iot.workflow.infrastructure.persistence.jpa;

import com.hkt.iot.workflow.infrastructure.persistence.po.WorkOrderTemplatePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 工单模板 JPA Repository
 *
 * @author HKT IoT Team
 */
@Repository
public interface WorkOrderTemplateJpaRepository extends JpaRepository<WorkOrderTemplatePO, String> {

    List<WorkOrderTemplatePO> findByTenantId(String tenantId);

    List<WorkOrderTemplatePO> findByType(String type);

    Optional<WorkOrderTemplatePO> findByTenantIdAndType(String tenantId, String type);
}
