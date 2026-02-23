package com.hkt.iot.workflow.infrastructure.persistence.jpa;

import com.hkt.iot.workflow.infrastructure.persistence.po.WorkOrderPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 工单 JPA Repository
 *
 * @author HKT IoT Team
 */
@Repository
public interface WorkOrderJpaRepository extends JpaRepository<WorkOrderPO, String> {

    Optional<WorkOrderPO> findByWorkOrderNo(String workOrderNo);

    List<WorkOrderPO> findByTenantId(String tenantId);

    List<WorkOrderPO> findByStatus(String status);

    List<WorkOrderPO> findByAssigneeId(String assigneeId);

    List<WorkOrderPO> findByReporterId(String reporterId);

    List<WorkOrderPO> findByTenantIdAndStatus(String tenantId, String status);

    List<WorkOrderPO> findBySpaceId(String spaceId);

    @Query("SELECT w FROM WorkOrderPO w WHERE w.dueTime < :now AND w.status NOT IN ('COMPLETED', 'CANCELLED', 'REJECTED')")
    List<WorkOrderPO> findOverdue(@Param("now") LocalDateTime now);

    @Query("SELECT w FROM WorkOrderPO w WHERE w.tenantId = :tenantId AND w.status IN :statuses")
    List<WorkOrderPO> findByTenantIdAndStatuses(@Param("tenantId") String tenantId, @Param("statuses") List<String> statuses);
}
