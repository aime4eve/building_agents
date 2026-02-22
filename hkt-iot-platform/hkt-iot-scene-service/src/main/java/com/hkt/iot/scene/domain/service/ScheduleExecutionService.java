package com.hkt.iot.scene.domain.service;

import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.scene.domain.model.Schedule;
import com.hkt.iot.scene.domain.model.ScheduleId;
import com.hkt.iot.scene.domain.model.ScheduleExecutionResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 定时计划执行领域服务接口
 *
 * 职责：处理定时计划的执行和调度
 */
public interface ScheduleExecutionService {

    /**
     * 执行定时计划
     *
     * @param scheduleId 定时计划ID
     * @return 执行结果
     */
    ScheduleExecutionResult execute(ScheduleId scheduleId);

    /**
     * 查找并执行到期的定时计划
     *
     * @param 执行时间
     * @return 执行结果列表
     */
    List<ScheduleExecutionResult> executeDueSchedules(LocalDateTime now);

    /**
     * 计算定时计划的下次执行时间
     *
     * @param scheduleId 定时计划ID
     */
    void calculateNextExecutionTime(ScheduleId scheduleId);

    /**
     * 激活定时计划调度
     *
     * @param scheduleId 定时计划ID
     */
    void activateSchedule(ScheduleId scheduleId);

    /**
     * 停用定时计划调度
     *
     * @param scheduleId 定时计划ID
     */
    void deactivateSchedule(ScheduleId scheduleId);

    /**
     * 获取租户下所有激活的定时计划
     *
     * @param tenantId 租户ID
     * @return 定时计划列表
     */
    List<Schedule> getActiveSchedules(TenantId tenantId);

    /**
     * 验证定时计划的有效性
     *
     * @param scheduleId 定时计划ID
     * @return 是否有效
     */
    boolean isScheduleValid(ScheduleId scheduleId);
}
