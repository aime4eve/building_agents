package com.hkt.iot.rule.infrastructure.persistence;

import com.hkt.iot.rule.domain.model.RuleExecutionLog;
import com.hkt.iot.rule.domain.repository.RuleExecutionLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 规则执行日志仓储实现
 * Phase 1: 简化实现，使用内存存储
 *
 * @author HKT IoT Team
 */
@Slf4j
@Repository
public class RuleExecutionLogRepositoryImpl implements RuleExecutionLogRepository {

    // Phase 1: 使用内存存储
    private final java.util.Map<Long, RuleExecutionLog> storage = new java.util.concurrent.ConcurrentHashMap<>();
    private volatile long idSequence = 1L;

    @Override
    public RuleExecutionLog save(RuleExecutionLog log) {
        if (log.getId() == null) {
            log.setId(idSequence++);
        }
        storage.put(log.getId(), log);
        log.debug("保存执行日志: id={}, ruleId={}", log.getId(), log.getRuleId());
        return log;
    }

    @Override
    public Optional<RuleExecutionLog> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<RuleExecutionLog> findByRuleId(Long ruleId) {
        return storage.values().stream()
                .filter(log -> ruleId.equals(log.getRuleId()))
                .toList();
    }

    @Override
    public List<RuleExecutionLog> findByTenantId(Long tenantId) {
        return storage.values().stream()
                .filter(log -> tenantId.equals(log.getTenantId()))
                .toList();
    }

    @Override
    public List<RuleExecutionLog> findByTenantIdAndTimeRange(
            Long tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        return storage.values().stream()
                .filter(log -> tenantId.equals(log.getTenantId()))
                .filter(log -> {
                    LocalDateTime execTime = log.getStartTime();
                    return !execTime.isBefore(startTime) && !execTime.isAfter(endTime);
                })
                .toList();
    }

    @Override
    public List<RuleExecutionLog> findFailedExecutions(Long tenantId, LocalDateTime since) {
        return storage.values().stream()
                .filter(log -> tenantId.equals(log.getTenantId()))
                .filter(log -> log.getExecutionStatus() == RuleExecutionLog.ExecutionStatus.FAILED)
                .filter(log -> log.getStartTime().isAfter(since))
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }

    @Override
    public void deleteByRuleId(Long ruleId) {
        storage.values().removeIf(log -> ruleId.equals(log.getRuleId()));
    }

    @Override
    public long countByRuleId(Long ruleId) {
        return storage.values().stream()
                .filter(log -> ruleId.equals(log.getRuleId()))
                .count();
    }

    @Override
    public long countByTenantId(Long tenantId) {
        return storage.values().stream()
                .filter(log -> tenantId.equals(log.getTenantId()))
                .count();
    }
}
