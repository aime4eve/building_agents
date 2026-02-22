package com.hkt.iot.rule.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.rule.domain.model.RuleExecutionLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则执行日志仓储接口
 * 基于DDD设计，提供规则执行日志实体的持久化操作
 *
 * @author HKT IoT Team
 */
public interface RuleExecutionLogRepository extends BaseRepository<RuleExecutionLog, Long> {

    /**
     * 根据执行ID查找
     *
     * @param executionId 执行ID
     * @return 执行日志
     */
    RuleExecutionLog findByExecutionId(String executionId);

    /**
     * 根据规则ID查找执行日志
     *
     * @param ruleId 规则ID
     * @param limit  限制数量
     * @return 执行日志列表
     */
    List<RuleExecutionLog> findByRuleIdOrderByTriggeredAtDesc(Long ruleId, int limit);

    /**
     * 根据租户ID和执行状态查找
     *
     * @param tenantId        租户ID
     * @param executionStatus 执行状态
     * @param limit           限制数量
     * @return 执行日志列表
     */
    List<RuleExecutionLog> findByTenantIdAndExecutionStatusOrderByTriggeredAtDesc(
            Long tenantId, RuleExecutionLog.ExecutionStatus executionStatus, int limit);

    /**
     * 根据时间范围查找执行日志
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 执行日志列表
     */
    List<RuleExecutionLog> findByTriggeredAtBetweenOrderByTriggeredAtDesc(
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据租户ID和时间范围查找
     *
     * @param tenantId  租户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 执行日志列表
     */
    List<RuleExecutionLog> findByTenantIdAndTriggeredAtBetweenOrderByTriggeredAtDesc(
            Long tenantId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计规则的执行次数
     *
     * @param ruleId 规则ID
     * @return 执行次数
     */
    long countByRuleId(Long ruleId);

    /**
     * 统计租户的执行次数（按状态）
     *
     * @param tenantId        租户ID
     * @param executionStatus 执行状态
     * @return 执行次数
     */
    long countByTenantIdAndExecutionStatus(Long tenantId, RuleExecutionLog.ExecutionStatus executionStatus);

    /**
     * 删除过期日志
     *
     * @param beforeTime 删除此时间之前的日志
     * @return 删除数量
     */
    long deleteByTriggeredAtBefore(LocalDateTime beforeTime);

    /**
     * 查找失败的执行日志
     *
     * @param tenantId 租户ID
     * @param limit    限制数量
     * @return 失败的执行日志列表
     */
    List<RuleExecutionLog> findFailedByTenantIdOrderByTriggeredAtDesc(Long tenantId, int limit);
}
