package com.hkt.iot.space.infrastructure.persistence;

import com.hkt.iot.domain.repository.OptimisticLockRepository;
import com.hkt.iot.space.domain.model.Space;
import com.hkt.iot.space.domain.repository.SpaceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

/**
 * 空间Repository实现
 * 基于DDL: space表
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
public class SpaceRepositoryImpl implements SpaceRepository, OptimisticLockRepository<Space, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Space save(Space space) {
        if (space.getId() == null) {
            entityManager.persist(space);
            log.debug("Created new space: id={}, code={}", space.getId(), space.getSpaceCode());
            return space;
        } else {
            Space merged = entityManager.merge(space);
            log.debug("Updated space: id={}, code={}", merged.getId(), merged.getSpaceCode());
            return merged;
        }
    }

    @Override
    public Optional<Space> findById(Long id) {
        Space space = entityManager.find(Space.class, id);
        if (space != null && space.getDeleted() == 0) {
            return Optional.of(space);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Space> findByTenantIdAndSpaceCode(Long tenantId, String spaceCode) {
        TypedQuery<Space> query = entityManager.createQuery(
            "SELECT s FROM Space s WHERE s.tenantId = :tenantId AND s.spaceCode = :spaceCode AND s.deleted = 0",
            Space.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("spaceCode", spaceCode);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Space> findByTenantId(Long tenantId) {
        TypedQuery<Space> query = entityManager.createQuery(
            "SELECT s FROM Space s WHERE s.tenantId = :tenantId AND s.deleted = 0 ORDER BY s.createdAt DESC",
            Space.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getResultList();
    }

    @Override
    public List<Space> findByTenantIdAndSpaceType(Long tenantId, String spaceType) {
        TypedQuery<Space> query = entityManager.createQuery(
            "SELECT s FROM Space s WHERE s.tenantId = :tenantId AND s.spaceType = :spaceType AND s.deleted = 0",
            Space.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("spaceType", spaceType);
        return query.getResultList();
    }

    @Override
    public List<Space> findByParentSpaceId(Long parentSpaceId) {
        TypedQuery<Space> query = entityManager.createQuery(
            "SELECT s FROM Space s WHERE s.parentSpaceId = :parentSpaceId AND s.deleted = 0 ORDER BY s.spaceLevel, s.spaceName",
            Space.class
        );
        query.setParameter("parentSpaceId", parentSpaceId);
        return query.getResultList();
    }

    @Override
    public List<Space> findRootSpacesByTenantId(Long tenantId) {
        TypedQuery<Space> query = entityManager.createQuery(
            "SELECT s FROM Space s WHERE s.tenantId = :tenantId AND s.parentSpaceId IS NULL AND s.deleted = 0",
            Space.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getResultList();
    }

    @Override
    @Transactional
    public void delete(Space space) {
        space.setDeleted(true);
        space.setDeletedAt(java.time.LocalDateTime.now());
        entityManager.merge(space);
        log.debug("Soft deleted space: id={}", space.getId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    public boolean existsByTenantIdAndSpaceCode(Long tenantId, String spaceCode) {
        return findByTenantIdAndSpaceCode(tenantId, spaceCode).isPresent();
    }

    @Override
    public long countByTenantId(Long tenantId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(s) FROM Space s WHERE s.tenantId = :tenantId AND s.deleted = 0",
            Long.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getSingleResult();
    }

    @Override
    public List<Space> findAll() {
        TypedQuery<Space> query = entityManager.createQuery(
            "SELECT s FROM Space s WHERE s.deleted = 0 ORDER BY s.createdAt DESC",
            Space.class
        );
        return query.getResultList();
    }

    @Override
    public Map<String, Long> countGroupByType(Long tenantId) {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT s.spaceType, COUNT(s) FROM Space s WHERE s.tenantId = :tenantId AND s.deleted = 0 GROUP BY s.spaceType",
            Object[].class
        );
        query.setParameter("tenantId", tenantId);
        
        Map<String, Long> result = new HashMap<>();
        for (Object[] row : query.getResultList()) {
            result.put(row[0].toString(), (Long) row[1]);
        }
        return result;
    }

    @Override
    public Map<String, Long> countGroupByStatus(Long tenantId) {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT s.spaceStatus, COUNT(s) FROM Space s WHERE s.tenantId = :tenantId AND s.deleted = 0 GROUP BY s.spaceStatus",
            Object[].class
        );
        query.setParameter("tenantId", tenantId);
        
        Map<String, Long> result = new HashMap<>();
        for (Object[] row : query.getResultList()) {
            result.put(row[0].toString(), (Long) row[1]);
        }
        return result;
    }

    @Override
    public Map<Integer, Long> countGroupByLevel(Long tenantId) {
        TypedQuery<Object[]> query = entityManager.createQuery(
            "SELECT s.spaceLevel, COUNT(s) FROM Space s WHERE s.tenantId = :tenantId AND s.deleted = 0 GROUP BY s.spaceLevel",
            Object[].class
        );
        query.setParameter("tenantId", tenantId);
        
        Map<Integer, Long> result = new HashMap<>();
        for (Object[] row : query.getResultList()) {
            result.put((Integer) row[0], (Long) row[1]);
        }
        return result;
    }
}
