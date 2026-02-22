package com.hkt.iot.device.domain.repository;

import com.hkt.iot.device.domain.model.OTATask;
import com.hkt.iot.device.domain.model.OTATask.TaskStatus;
import com.hkt.iot.domain.repository.OptimisticLockRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * OTA任务仓储接口
 *
 * @author HKT IoT Team
 */
public interface OTATaskRepository extends OptimisticLockRepository<OTATask, Long> {

    /**
     * 根据任务编码查找
     *
     * @param tenantId 租户ID
     * @param taskCode 任务编码
     * @return OTA任务
     */
    Optional<OTATask> findByTenantIdAndTaskCode(Long tenantId, String taskCode);

    /**
     * 根据租户ID查找所有任务
     *
     * @param tenantId 租户ID
     * @return 任务列表
     */
    List<OTATask> findByTenantId(Long tenantId);

    /**
     * 根据任务状态查找
     *
     * @param tenantId   租户ID
     * @param taskStatus 任务状态
     * @return 任务列表
     */
    List<OTATask> findByTenantIdAndTaskStatus(Long tenantId, TaskStatus taskStatus);

    /**
     * 查找待执行的任务
     *
     * @param scheduledTime 调度时间
     * @return 任务列表
     */
    List<OTATask> findByTaskStatusAndScheduledTimeBefore(TaskStatus taskStatus, LocalDateTime scheduledTime);

    /**
     * 查找执行中的超时任务
     *
     * @param startedTime 开始时间阈值
     * @return 任务列表
     */
    List<OTATask> findByTaskStatusAndStartedTimeBefore(TaskStatus taskStatus, LocalDateTime startedTime);

    /**
     * 统计租户下的任务数量
     *
     * @param tenantId 租户ID
     * @return 任务数量
     */
    long countByTenantId(Long tenantId);

    /**
     * 检查任务编码是否存在
     *
     * @param tenantId 租户ID
     * @param taskCode 任务编码
     * @return 是否存在
     */
    boolean existsByTenantIdAndTaskCode(Long tenantId, String taskCode);
}
