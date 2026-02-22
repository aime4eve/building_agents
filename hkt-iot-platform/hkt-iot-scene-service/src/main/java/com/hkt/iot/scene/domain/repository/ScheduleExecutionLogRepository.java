package com.hkt.iot.scene.domain.repository;

import com.hkt.iot.scene.domain.model.ExecutionId;
import com.hkt.iot.scene.domain.model.ScheduleExecutionLog;
import com.hkt.iot.scene.domain.model.ScheduleId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 定时计划执行日志仓储接口
 *
 * 职责：管理定时计划执行日志的持久化
 */
public interface ScheduleExecutionLogRepository {

    /**
     * 保存定时计划执行日志
     */
    ScheduleExecutionLog save(ScheduleExecutionLog log);

    /**
     * 根据ID查找定时计划执行日志
     */
    Optional<ScheduleExecutionLog> findById(ExecutionId id);

    /**
     * 根据定时计划ID查找执行日志列表
     */
    List<ScheduleExecutionLog> findBySchedule(ScheduleId scheduleId);

    /**
     * 根据时间范围查找执行日志列表
     */
    List<ScheduleExecutionLog> findByTimeRange(LocalDateTime from, LocalDateTime to);

    /**
     * 根据定时计划ID和时间范围查找执行日志列表
     */
    List<ScheduleExecutionLog> findByScheduleAndTimeRange(ScheduleId scheduleId, LocalDateTime from, LocalDateTime to);

    /**
     * 根据执行结果查找执行日志列表
     */
    List<ScheduleExecutionLog> findByResult(com.hkt.iot.scene.domain.model.ExecutionResult result);

    /**
     * 删除执行日志
     */
    void delete(ScheduleExecutionLog log);

    /**
     * 根据ID删除执行日志
     */
    void deleteById(ExecutionId id);

    /**
     * 删除指定时间之前的日志
     */
    int deleteByExecutedAtBefore(LocalDateTime before);
}
