package com.hkt.iot.scene.application.service;

import com.hkt.iot.domain.shared.SpaceId;
import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.scene.application.dto.*;
import com.hkt.iot.scene.domain.model.ScheduleId;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时计划应用服务
 *
 * 职责：协调领域对象完成业务用例
 */
public interface ScheduleApplicationService {

    /**
     * 创建定时计划
     */
    ScheduleDTO createSchedule(CreateScheduleRequest request, TenantId tenantId);

    /**
     * 更新定时计划
     */
    ScheduleDTO updateSchedule(ScheduleId scheduleId, UpdateScheduleRequest request);

    /**
     * 删除定时计划
     */
    void deleteSchedule(ScheduleId scheduleId);

    /**
     * 获取定时计划详情
     */
    ScheduleDTO getSchedule(ScheduleId scheduleId);

    /**
     * 获取租户下的定时计划列表
     */
    List<ScheduleDTO> getSchedulesByTenant(TenantId tenantId);

    /**
     * 获取空间下的定时计划列表
     */
    List<ScheduleDTO> getSchedulesBySpace(SpaceId spaceId);

    /**
     * 获取待执行的定时计划列表
     */
    List<ScheduleDTO> getPendingSchedules(LocalDateTime beforeTime);

    /**
     * 激活定时计划
     */
    void activateSchedule(ScheduleId scheduleId);

    /**
     * 停用定时计划
     */
    void deactivateSchedule(ScheduleId scheduleId);

    /**
     * 更新Cron表达式
     */
    void updateCronExpression(ScheduleId scheduleId, CronExpression cronExpression);

    /**
     * 添加执行动作
     */
    void addAction(ScheduleId scheduleId, SceneActionDTO action);

    /**
     * 移除执行动作
     */
    void removeAction(ScheduleId scheduleId, ActionId actionId);

    /**
     * 手动执行定时计划（测试用）
     */
    ScheduleExecutionResultDTO executeSchedule(ScheduleId scheduleId);

    /**
     * 获取定时计划执行日志
     */
    List<ScheduleExecutionLogDTO> getScheduleExecutionLogs(ScheduleId scheduleId, int page, int size);
}
