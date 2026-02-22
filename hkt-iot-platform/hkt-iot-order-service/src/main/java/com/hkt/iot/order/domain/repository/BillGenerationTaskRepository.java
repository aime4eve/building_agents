package com.hkt.iot.order.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.order.domain.model.BillGenerationTask;
import com.hkt.iot.order.domain.model.EnergyType;

import java.util.List;
import java.util.Optional;

/**
 * 账单生成任务仓储接口
 *
 * @author HKT IoT Team
 */
public interface BillGenerationTaskRepository extends BaseRepository<BillGenerationTask, Long> {

    /**
     * 根据任务编号查找
     */
    Optional<BillGenerationTask> findByTaskNo(String taskNo);

    /**
     * 根据租户ID查找任务
     */
    List<BillGenerationTask> findByTenantId(Long tenantId);

    /**
     * 根据状态查找任务
     */
    List<BillGenerationTask> findByTaskStatus(BillGenerationTask.TaskStatus taskStatus);

    /**
     * 查找指定账单周期的任务
     */
    List<BillGenerationTask> findByBillingYearAndBillingMonth(Integer year, Integer month);

    /**
     * 查找租户指定账单周期的任务
     */
    Optional<BillGenerationTask> findByTenantIdAndBillingYearAndBillingMonthAndEnergyType(
            Long tenantId, Integer year, Integer month, EnergyType energyType);

    /**
     * 检查任务编号是否存在
     */
    boolean existsByTaskNo(String taskNo);

    /**
     * 查找正在执行的任务
     */
    List<BillGenerationTask> findRunningTasks();
}
