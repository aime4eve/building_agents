package com.hkt.iot.workflow.domain.repository;

import com.hkt.iot.workflow.domain.model.entity.SLAMonitor;
import com.hkt.iot.workflow.domain.model.valueobject.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SLA 监控仓储接口
 *
 * @author HKT IoT Team
 */
public interface SLAMonitorRepository {

    /**
     * 保存 SLA 监控记录
     */
    SLAMonitor save(SLAMonitor monitor);

    /**
     * 根据 ID 查找 SLA 监控记录
     */
    Optional<SLAMonitor> findById(SLAMonitorId id);

    /**
     * 根据流程实例 ID 查找 SLA 监控记录
     */
    Optional<SLAMonitor> findByProcessInstanceId(ProcessInstanceId processInstanceId);

    /**
     * 根据任务 ID 查找 SLA 监控记录
     */
    Optional<SLAMonitor> findByTaskId(TaskId taskId);

    /**
     * 根据租户 ID 和时间范围查找 SLA 监控记录列表
     */
    List<SLAMonitor> findByTenantIdAndCreatedAtBetween(
            TenantId tenantId,
            LocalDateTime startTime,
            LocalDateTime endTime);

    /**
     * 根据租户 ID 和 SLA 状态查找监控记录列表
     */
    List<SLAMonitor> findByTenantIdAndResponseStatus(TenantId tenantId, SLAStatus status);

    /**
     * 删除 SLA 监控记录
     */
    void delete(SLAMonitor monitor);
}
