package com.hkt.iot.space.infrastructure.persistence;

import com.hkt.iot.domain.repository.OptimisticLockRepository;
import com.hkt.iot.space.domain.model.LogicalSpaceGroup;
import com.hkt.iot.space.domain.repository.LogicalSpaceGroupRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 逻辑空间分组Repository实现
 * 基于DDL: logical_space_group表
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
public class LogicalSpaceGroupRepositoryImpl implements LogicalSpaceGroupRepository, OptimisticLockRepository<LogicalSpaceGroup, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public LogicalSpaceGroup save(LogicalSpaceGroup group) {
        if (group.getId() == null) {
            entityManager.persist(group);
            log.debug("Created new logical space group: id={}, code={}", group.getId(), group.getGroupCode());
            return group;
        } else {
            LogicalSpaceGroup merged = entityManager.merge(group);
            log.debug("Updated logical space group: id={}, code={}", merged.getId(), merged.getGroupCode());
            return merged;
        }
    }

    @Override
    public List<LogicalSpaceGroup> saveAll(List<LogicalSpaceGroup> entities) {
        return entities.stream()
                .map(this::save)
                .toList();
    }

    @Override
    public Optional<LogicalSpaceGroup> findById(Long id) {
        LogicalSpaceGroup group = entityManager.find(LogicalSpaceGroup.class, id);
        if (group != null && !group.getDeleted()) {
            return Optional.of(group);
        }
        return Optional.empty();
    }

    @Override
    public List<LogicalSpaceGroup> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        TypedQuery<LogicalSpaceGroup> query = entityManager.createQuery(
                "SELECT g FROM LogicalSpaceGroup g WHERE g.id IN :ids AND g.deleted = false",
                LogicalSpaceGroup.class
        );
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    @Override
    public List<LogicalSpaceGroup> findAll() {
        TypedQuery<LogicalSpaceGroup> query = entityManager.createQuery(
                "SELECT g FROM LogicalSpaceGroup g WHERE g.deleted = false ORDER BY g.createdAt DESC",
                LogicalSpaceGroup.class
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
                "SELECT COUNT(g) FROM LogicalSpaceGroup g WHERE g.deleted = false",
                Long.class
        );
        return query.getSingleResult();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    @Transactional
    public void delete(LogicalSpaceGroup group) {
        group.setDeleted(true);
        group.setDeletedAt(java.time.LocalDateTime.now());
        entityManager.merge(group);
        log.debug("Soft deleted logical space group: id={}", group.getId());
    }

    @Override
    @Transactional
    public void deleteAll(List<LogicalSpaceGroup> entities) {
        entities.forEach(this::delete);
    }

    @Override
    @Transactional
    public void deleteAll() {
        List<LogicalSpaceGroup> allGroups = findAll();
        allGroups.forEach(this::delete);
    }

    @Override
    public Optional<LogicalSpaceGroup> findByTenantIdAndGroupCode(Long tenantId, String groupCode) {
        TypedQuery<LogicalSpaceGroup> query = entityManager.createQuery(
                "SELECT g FROM LogicalSpaceGroup g WHERE g.tenantId = :tenantId AND g.groupCode = :groupCode AND g.deleted = false",
                LogicalSpaceGroup.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("groupCode", groupCode);

        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<LogicalSpaceGroup> findByTenantIdAndGroupType(Long tenantId, LogicalSpaceGroup.GroupType groupType) {
        TypedQuery<LogicalSpaceGroup> query = entityManager.createQuery(
                "SELECT g FROM LogicalSpaceGroup g WHERE g.tenantId = :tenantId AND g.groupType = :groupType AND g.deleted = false ORDER BY g.displayOrder, g.createdAt DESC",
                LogicalSpaceGroup.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("groupType", groupType);
        return query.getResultList();
    }

    @Override
    public List<LogicalSpaceGroup> findActiveByTenantId(Long tenantId) {
        TypedQuery<LogicalSpaceGroup> query = entityManager.createQuery(
                "SELECT g FROM LogicalSpaceGroup g WHERE g.tenantId = :tenantId AND g.status = 'ACTIVE' AND g.deleted = false ORDER BY g.displayOrder, g.createdAt DESC",
                LogicalSpaceGroup.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getResultList();
    }

    @Override
    public List<LogicalSpaceGroup> findByTenantId(Long tenantId) {
        TypedQuery<LogicalSpaceGroup> query = entityManager.createQuery(
                "SELECT g FROM LogicalSpaceGroup g WHERE g.tenantId = :tenantId AND g.deleted = false ORDER BY g.displayOrder, g.createdAt DESC",
                LogicalSpaceGroup.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getResultList();
    }

    @Override
    public boolean existsByTenantIdAndGroupCode(Long tenantId, String groupCode) {
        return findByTenantIdAndGroupCode(tenantId, groupCode).isPresent();
    }

    @Override
    public long countByTenantId(Long tenantId) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(g) FROM LogicalSpaceGroup g WHERE g.tenantId = :tenantId AND g.deleted = false",
                Long.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getSingleResult();
    }

    @Override
    public void refresh(LogicalSpaceGroup entity) {
        entityManager.refresh(entity);
    }

    @Override
    public Long getCurrentVersion(Long id) {
        Optional<LogicalSpaceGroup> group = findById(id);
        return group.map(LogicalSpaceGroup::getVersion).orElse(null);
    }

    @Override
    public Long getId(LogicalSpaceGroup entity) {
        return entity.getId();
    }

    @Override
    public Long getVersion(LogicalSpaceGroup entity) {
        return entity.getVersion();
    }
}
