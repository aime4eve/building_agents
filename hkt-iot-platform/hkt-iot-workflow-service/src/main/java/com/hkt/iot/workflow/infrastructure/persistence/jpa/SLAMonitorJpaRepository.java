package com.hkt.iot.workflow.infrastructure.persistence.jpa;

import com.hkt.iot.workflow.infrastructure.persistence.po.SLAMonitorPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SLA 监控记录 JPA Repository
 *
 * @author HKT IoT Team
 */
@Repository
public interface SLAMonitorJpaRepository extends JpaRepository<SLAMonitorPO, String> {

    /**
     * 根据流程实例 ID 查找 SLA 监控记录
     */
    Optional<SLAMonitorPO> findByProcessInstanceId(String processInstanceId);

    /**
     * 根据任务 ID 查找 SLA 监控记录
     */
    Optional<SLAMonitorPO> findByTaskId(String taskId);

    /**
     * 根据租户 ID 和时间范围查找 SLA 监控记录列表
     */
    List<SLAMonitorPO> findByTenantIdAndCreatedAtBetween(
            String tenantId,
            LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 根据租户 ID 和 SLA 状态查找监控记录列表
     */
    List<SLAMonitorPO> findByTenantIdAndResponseStatus(String tenantId, String status);

    /**
     * 根据流程实例 ID 和任务 ID 查找
     */
    Optional<SLAMonitorPO> findByProcessInstanceIdAndTaskId(String processInstanceId, String taskId);
}
