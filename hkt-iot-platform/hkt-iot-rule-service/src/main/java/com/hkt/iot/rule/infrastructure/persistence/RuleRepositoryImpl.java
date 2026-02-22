package com.hkt.iot.rule.infrastructure.persistence;

import com.hkt.iot.domain.repository.OptimisticLockRepository;
import com.hkt.iot.rule.domain.model.Rule;
import com.hkt.iot.rule.domain.repository.RuleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 规则Repository实现
 * 基于DDL: rule表
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
public class RuleRepositoryImpl implements RuleRepository, OptimisticLockRepository<Rule, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Rule save(Rule rule) {
        if (rule.getId() == null) {
            entityManager.persist(rule);
            log.debug("Created new rule: id={}, code={}", rule.getId(), rule.getRuleCode());
            return rule;
        } else {
            Rule merged = entityManager.merge(rule);
            log.debug("Updated rule: id={}, code={}", merged.getId(), merged.getRuleCode());
            return merged;
        }
    }

    @Override
    public Optional<Rule> findById(Long id) {
        Rule rule = entityManager.find(Rule.class, id);
        if (rule != null && rule.getDeleted() == 0) {
            return Optional.of(rule);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Rule> findByTenantIdAndRuleCode(Long tenantId, String ruleCode) {
        TypedQuery<Rule> query = entityManager.createQuery(
            "SELECT r FROM Rule r WHERE r.tenantId = :tenantId AND r.ruleCode = :ruleCode AND r.deleted = 0",
            Rule.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("ruleCode", ruleCode);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Rule> findByTenantId(Long tenantId) {
        TypedQuery<Rule> query = entityManager.createQuery(
            "SELECT r FROM Rule r WHERE r.tenantId = :tenantId AND r.deleted = 0 ORDER BY r.createdAt DESC",
            Rule.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getResultList();
    }

    @Override
    public List<Rule> findByTenantIdAndRuleType(Long tenantId, String ruleType) {
        TypedQuery<Rule> query = entityManager.createQuery(
            "SELECT r FROM Rule r WHERE r.tenantId = :tenantId AND r.ruleType = :ruleType AND r.deleted = 0",
            Rule.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("ruleType", ruleType);
        return query.getResultList();
    }

    @Override
    public List<Rule> findByTenantIdAndRuleStatus(Long tenantId, String ruleStatus) {
        TypedQuery<Rule> query = entityManager.createQuery(
            "SELECT r FROM Rule r WHERE r.tenantId = :tenantId AND r.ruleStatus = :ruleStatus AND r.deleted = 0",
            Rule.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("ruleStatus", ruleStatus);
        return query.getResultList();
    }

    @Override
    public List<Rule> findActiveRulesByTenantId(Long tenantId) {
        TypedQuery<Rule> query = entityManager.createQuery(
            "SELECT r FROM Rule r WHERE r.tenantId = :tenantId AND r.ruleStatus = 'ACTIVE' AND r.isEnabled = 1 AND r.deleted = 0",
            Rule.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getResultList();
    }

    @Override
    public List<Rule> findBySpaceId(Long spaceId) {
        TypedQuery<Rule> query = entityManager.createQuery(
            "SELECT r FROM Rule r WHERE r.spaceId = :spaceId AND r.deleted = 0",
            Rule.class
        );
        query.setParameter("spaceId", spaceId);
        return query.getResultList();
    }

    @Override
    public List<Rule> findByDeviceId(Long deviceId) {
        // This would require JSON_CONTAINS or similar for the device_ids JSON field
        // For now, return empty list as this needs custom implementation
        return List.of();
    }

    @Override
    @Transactional
    public void updateRuleStatus(Long ruleId, String ruleStatus) {
        Rule rule = findById(ruleId).orElse(null);
        if (rule != null) {
            rule.setRuleStatus(ruleStatus);
            entityManager.merge(rule);
            log.debug("Updated rule status: id={}, status={}", ruleId, ruleStatus);
        }
    }

    @Override
    @Transactional
    public void updateIsEnabled(Long ruleId, boolean isEnabled) {
        Rule rule = findById(ruleId).orElse(null);
        if (rule != null) {
            rule.setIsEnabled(isEnabled ? (byte) 1 : (byte) 0);
            entityManager.merge(rule);
            log.debug("Updated rule enabled: id={}, enabled={}", ruleId, isEnabled);
        }
    }

    @Override
    @Transactional
    public void incrementExecutionCount(Long ruleId, boolean success) {
        Rule rule = findById(ruleId).orElse(null);
        if (rule != null) {
            rule.setTotalExecutions(rule.getTotalExecutions() + 1);
            if (success) {
                rule.setSuccessExecutions(rule.getSuccessExecutions() + 1);
            } else {
                rule.setFailedExecutions(rule.getFailedExecutions() + 1);
            }
            rule.setLastExecutionTime(java.time.LocalDateTime.now());
            entityManager.merge(rule);
        }
    }

    @Override
    @Transactional
    public void delete(Rule rule) {
        rule.setDeleted(true);
        rule.setDeletedAt(java.time.LocalDateTime.now());
        entityManager.merge(rule);
        log.debug("Soft deleted rule: id={}", rule.getId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    public boolean existsByTenantIdAndRuleCode(Long tenantId, String ruleCode) {
        return findByTenantIdAndRuleCode(tenantId, ruleCode).isPresent();
    }

    @Override
    public long countByTenantId(Long tenantId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(r) FROM Rule r WHERE r.tenantId = :tenantId AND r.deleted = 0",
            Long.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getSingleResult();
    }

    @Override
    public List<Rule> findAll() {
        TypedQuery<Rule> query = entityManager.createQuery(
            "SELECT r FROM Rule r WHERE r.deleted = 0 ORDER BY r.createdAt DESC",
            Rule.class
        );
        return query.getResultList();
    }
}
