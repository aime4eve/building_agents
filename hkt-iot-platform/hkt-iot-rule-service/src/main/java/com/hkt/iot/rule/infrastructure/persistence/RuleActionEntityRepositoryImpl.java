package com.hkt.iot.rule.infrastructure.persistence;

import com.hkt.iot.rule.domain.model.RuleActionEntity;
import com.hkt.iot.rule.domain.repository.RuleActionEntityRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 规则动作Repository实现
 * 基于DDL: rule_action表
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
public class RuleActionEntityRepositoryImpl implements RuleActionEntityRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public RuleActionEntity save(RuleActionEntity action) {
        if (action.getId() == null) {
            entityManager.persist(action);
            log.debug("Created new rule action: id={}, actionCode={}", action.getId(), action.getActionCode());
            return action;
        } else {
            RuleActionEntity merged = entityManager.merge(action);
            log.debug("Updated rule action: id={}, actionCode={}", merged.getId(), merged.getActionCode());
            return merged;
        }
    }

    @Override
    public List<RuleActionEntity> saveAll(List<RuleActionEntity> entities) {
        return entities.stream()
                .map(this::save)
                .toList();
    }

    @Override
    public Optional<RuleActionEntity> findById(Long id) {
        RuleActionEntity action = entityManager.find(RuleActionEntity.class, id);
        // RuleActionEntity没有deleted字段，直接返回
        return Optional.ofNullable(action);
    }

    @Override
    public List<RuleActionEntity> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        TypedQuery<RuleActionEntity> query = entityManager.createQuery(
            "SELECT ra FROM RuleActionEntity ra WHERE ra.id IN :ids",
            RuleActionEntity.class
        );
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    @Override
    public List<RuleActionEntity> findAll() {
        TypedQuery<RuleActionEntity> query = entityManager.createQuery(
            "SELECT ra FROM RuleActionEntity ra ORDER BY ra.createdAt DESC",
            RuleActionEntity.class
        );
        return query.getResultList();
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    @Override
    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(ra) FROM RuleActionEntity ra",
            Long.class
        );
        return query.getSingleResult();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(entityManager::remove);
    }

    @Override
    @Transactional
    public void delete(RuleActionEntity action) {
        entityManager.remove(action);
        log.debug("Deleted rule action: id={}", action.getId());
    }

    @Override
    public void deleteAll(List<RuleActionEntity> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        findAll().forEach(this::delete);
    }

    @Override
    public List<RuleActionEntity> findByRuleIdOrderByActionOrderAsc(Long ruleId) {
        TypedQuery<RuleActionEntity> query = entityManager.createQuery(
            "SELECT ra FROM RuleActionEntity ra WHERE ra.ruleId = :ruleId ORDER BY ra.actionOrder ASC",
            RuleActionEntity.class
        );
        query.setParameter("ruleId", ruleId);
        return query.getResultList();
    }

    @Override
    public List<RuleActionEntity> findByTenantIdAndRuleId(Long tenantId, Long ruleId) {
        TypedQuery<RuleActionEntity> query = entityManager.createQuery(
            "SELECT ra FROM RuleActionEntity ra WHERE ra.tenantId = :tenantId AND ra.ruleId = :ruleId ORDER BY ra.actionOrder ASC",
            RuleActionEntity.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("ruleId", ruleId);
        return query.getResultList();
    }

    @Override
    public List<RuleActionEntity> findByTargetDeviceId(Long targetDeviceId) {
        TypedQuery<RuleActionEntity> query = entityManager.createQuery(
            "SELECT ra FROM RuleActionEntity ra WHERE ra.targetDeviceId = :targetDeviceId ORDER BY ra.createdAt DESC",
            RuleActionEntity.class
        );
        query.setParameter("targetDeviceId", targetDeviceId);
        return query.getResultList();
    }

    @Override
    public List<RuleActionEntity> findByActionType(RuleActionEntity.ActionType actionType) {
        TypedQuery<RuleActionEntity> query = entityManager.createQuery(
            "SELECT ra FROM RuleActionEntity ra WHERE ra.actionType = :actionType ORDER BY ra.createdAt DESC",
            RuleActionEntity.class
        );
        query.setParameter("actionType", actionType);
        return query.getResultList();
    }

    @Override
    public long countByRuleId(Long ruleId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(ra) FROM RuleActionEntity ra WHERE ra.ruleId = :ruleId",
            Long.class
        );
        query.setParameter("ruleId", ruleId);
        return query.getSingleResult();
    }

    @Override
    @Transactional
    public void deleteByRuleId(Long ruleId) {
        TypedQuery<RuleActionEntity> query = entityManager.createQuery(
            "SELECT ra FROM RuleActionEntity ra WHERE ra.ruleId = :ruleId",
            RuleActionEntity.class
        );
        query.setParameter("ruleId", ruleId);
        List<RuleActionEntity> actions = query.getResultList();
        actions.forEach(this::delete);
        log.debug("Deleted all actions for rule: ruleId={}", ruleId);
    }
}
