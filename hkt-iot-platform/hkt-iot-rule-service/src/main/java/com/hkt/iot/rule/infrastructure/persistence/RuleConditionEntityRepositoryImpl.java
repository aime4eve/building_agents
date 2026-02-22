package com.hkt.iot.rule.infrastructure.persistence;

import com.hkt.iot.rule.domain.model.RuleConditionEntity;
import com.hkt.iot.rule.domain.repository.RuleConditionEntityRepository;
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
 * 规则条件Repository实现
 * 基于DDL: rule_condition表
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
public class RuleConditionEntityRepositoryImpl implements RuleConditionEntityRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public RuleConditionEntity save(RuleConditionEntity condition) {
        if (condition.getId() == null) {
            entityManager.persist(condition);
            log.debug("Created new rule condition: id={}, conditionCode={}", condition.getId(), condition.getConditionCode());
            return condition;
        } else {
            RuleConditionEntity merged = entityManager.merge(condition);
            log.debug("Updated rule condition: id={}, conditionCode={}", merged.getId(), merged.getConditionCode());
            return merged;
        }
    }

    @Override
    public List<RuleConditionEntity> saveAll(List<RuleConditionEntity> entities) {
        return entities.stream()
                .map(this::save)
                .toList();
    }

    @Override
    public Optional<RuleConditionEntity> findById(Long id) {
        RuleConditionEntity condition = entityManager.find(RuleConditionEntity.class, id);
        // RuleConditionEntity没有deleted字段，直接返回
        return Optional.ofNullable(condition);
    }

    @Override
    public List<RuleConditionEntity> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        TypedQuery<RuleConditionEntity> query = entityManager.createQuery(
            "SELECT rc FROM RuleConditionEntity rc WHERE rc.id IN :ids",
            RuleConditionEntity.class
        );
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    @Override
    public List<RuleConditionEntity> findAll() {
        TypedQuery<RuleConditionEntity> query = entityManager.createQuery(
            "SELECT rc FROM RuleConditionEntity rc ORDER BY rc.createdAt DESC",
            RuleConditionEntity.class
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
            "SELECT COUNT(rc) FROM RuleConditionEntity rc",
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
    public void delete(RuleConditionEntity condition) {
        entityManager.remove(condition);
        log.debug("Deleted rule condition: id={}", condition.getId());
    }

    @Override
    public void deleteAll(List<RuleConditionEntity> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        findAll().forEach(this::delete);
    }

    @Override
    public List<RuleConditionEntity> findByRuleIdOrderByConditionOrderAsc(Long ruleId) {
        TypedQuery<RuleConditionEntity> query = entityManager.createQuery(
            "SELECT rc FROM RuleConditionEntity rc WHERE rc.ruleId = :ruleId ORDER BY rc.conditionOrder ASC",
            RuleConditionEntity.class
        );
        query.setParameter("ruleId", ruleId);
        return query.getResultList();
    }

    @Override
    public List<RuleConditionEntity> findByTenantIdAndRuleId(Long tenantId, Long ruleId) {
        TypedQuery<RuleConditionEntity> query = entityManager.createQuery(
            "SELECT rc FROM RuleConditionEntity rc WHERE rc.tenantId = :tenantId AND rc.ruleId = :ruleId ORDER BY rc.conditionOrder ASC",
            RuleConditionEntity.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("ruleId", ruleId);
        return query.getResultList();
    }

    @Override
    public List<RuleConditionEntity> findByDeviceId(Long deviceId) {
        TypedQuery<RuleConditionEntity> query = entityManager.createQuery(
            "SELECT rc FROM RuleConditionEntity rc WHERE rc.deviceId = :deviceId ORDER BY rc.createdAt DESC",
            RuleConditionEntity.class
        );
        query.setParameter("deviceId", deviceId);
        return query.getResultList();
    }

    @Override
    public long countByRuleId(Long ruleId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(rc) FROM RuleConditionEntity rc WHERE rc.ruleId = :ruleId",
            Long.class
        );
        query.setParameter("ruleId", ruleId);
        return query.getSingleResult();
    }

    @Override
    @Transactional
    public void deleteByRuleId(Long ruleId) {
        TypedQuery<RuleConditionEntity> query = entityManager.createQuery(
            "SELECT rc FROM RuleConditionEntity rc WHERE rc.ruleId = :ruleId",
            RuleConditionEntity.class
        );
        query.setParameter("ruleId", ruleId);
        List<RuleConditionEntity> conditions = query.getResultList();
        conditions.forEach(this::delete);
        log.debug("Deleted all conditions for rule: ruleId={}", ruleId);
    }
}
