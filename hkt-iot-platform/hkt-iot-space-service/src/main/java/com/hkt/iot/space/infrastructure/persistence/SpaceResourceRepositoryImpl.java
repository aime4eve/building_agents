package com.hkt.iot.space.infrastructure.persistence;

import com.hkt.iot.space.domain.model.SpaceResource;
import com.hkt.iot.space.domain.repository.SpaceResourceRepository;
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
 * 空间资源Repository实现
 * 基于DDL: space_resource表
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
public class SpaceResourceRepositoryImpl implements SpaceResourceRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public SpaceResource save(SpaceResource spaceResource) {
        if (spaceResource.getId() == null) {
            entityManager.persist(spaceResource);
            log.debug("Created new space resource: id={}, spaceId={}, resourceType={}",
                    spaceResource.getId(), spaceResource.getSpaceId(), spaceResource.getResourceType());
            return spaceResource;
        } else {
            spaceResource.setUpdatedAt(LocalDateTime.now());
            SpaceResource merged = entityManager.merge(spaceResource);
            log.debug("Updated space resource: id={}, spaceId={}, resourceType={}",
                    merged.getId(), merged.getSpaceId(), merged.getResourceType());
            return merged;
        }
    }

    @Override
    public List<SpaceResource> saveAll(List<SpaceResource> entities) {
        return entities.stream()
                .map(this::save)
                .toList();
    }

    @Override
    public Optional<SpaceResource> findById(Long id) {
        SpaceResource spaceResource = entityManager.find(SpaceResource.class, id);
        if (spaceResource != null && !spaceResource.getDeleted()) {
            return Optional.of(spaceResource);
        }
        return Optional.empty();
    }

    @Override
    public List<SpaceResource> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        TypedQuery<SpaceResource> query = entityManager.createQuery(
                "SELECT sr FROM SpaceResource sr WHERE sr.id IN :ids AND sr.deleted = false",
                SpaceResource.class
        );
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    @Override
    public List<SpaceResource> findAll() {
        TypedQuery<SpaceResource> query = entityManager.createQuery(
                "SELECT sr FROM SpaceResource sr WHERE sr.deleted = false ORDER BY sr.createdAt DESC",
                SpaceResource.class
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
                "SELECT COUNT(sr) FROM SpaceResource sr WHERE sr.deleted = false",
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
    public void delete(SpaceResource spaceResource) {
        spaceResource.setDeleted(true);
        spaceResource.setDeletedAt(LocalDateTime.now());
        entityManager.merge(spaceResource);
        log.debug("Soft deleted space resource: id={}", spaceResource.getId());
    }

    @Override
    @Transactional
    public void deleteAll(List<SpaceResource> entities) {
        entities.forEach(this::delete);
    }

    @Override
    @Transactional
    public void deleteAll() {
        TypedQuery<SpaceResource> query = entityManager.createQuery(
                "SELECT sr FROM SpaceResource sr WHERE sr.deleted = false",
                SpaceResource.class
        );
        query.getResultList().forEach(this::delete);
    }

    @Override
    public List<SpaceResource> findBySpaceId(Long spaceId) {
        TypedQuery<SpaceResource> query = entityManager.createQuery(
                "SELECT sr FROM SpaceResource sr WHERE sr.spaceId = :spaceId AND sr.deleted = false ORDER BY sr.createdAt DESC",
                SpaceResource.class
        );
        query.setParameter("spaceId", spaceId);
        return query.getResultList();
    }

    @Override
    public List<SpaceResource> findByTenantIdAndResourceType(Long tenantId, SpaceResource.ResourceType resourceType) {
        TypedQuery<SpaceResource> query = entityManager.createQuery(
                "SELECT sr FROM SpaceResource sr WHERE sr.tenantId = :tenantId AND sr.resourceType = :resourceType AND sr.deleted = false ORDER BY sr.createdAt DESC",
                SpaceResource.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("resourceType", resourceType);
        return query.getResultList();
    }

    @Override
    public List<SpaceResource> findByResourceTypeAndResourceId(SpaceResource.ResourceType resourceType, Long resourceId) {
        TypedQuery<SpaceResource> query = entityManager.createQuery(
                "SELECT sr FROM SpaceResource sr WHERE sr.resourceType = :resourceType AND sr.resourceId = :resourceId AND sr.deleted = false ORDER BY sr.createdAt DESC",
                SpaceResource.class
        );
        query.setParameter("resourceType", resourceType);
        query.setParameter("resourceId", resourceId);
        return query.getResultList();
    }

    @Override
    public List<SpaceResource> findByTenantIdAndSpaceIdAndResourceType(
            Long tenantId, Long spaceId, SpaceResource.ResourceType resourceType) {
        TypedQuery<SpaceResource> query = entityManager.createQuery(
                "SELECT sr FROM SpaceResource sr WHERE sr.tenantId = :tenantId AND sr.spaceId = :spaceId AND sr.resourceType = :resourceType AND sr.deleted = false ORDER BY sr.createdAt DESC",
                SpaceResource.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("spaceId", spaceId);
        query.setParameter("resourceType", resourceType);
        return query.getResultList();
    }

    @Override
    public List<SpaceResource> findPrimaryBySpaceId(Long spaceId) {
        TypedQuery<SpaceResource> query = entityManager.createQuery(
                "SELECT sr FROM SpaceResource sr WHERE sr.spaceId = :spaceId AND sr.primaryRelation = true AND sr.deleted = false ORDER BY sr.createdAt DESC",
                SpaceResource.class
        );
        query.setParameter("spaceId", spaceId);
        return query.getResultList();
    }

    @Override
    public List<SpaceResource> findValidByResourceIdAndResourceType(
            Long resourceId, SpaceResource.ResourceType resourceType) {
        LocalDateTime now = LocalDateTime.now();
        TypedQuery<SpaceResource> query = entityManager.createQuery(
                "SELECT sr FROM SpaceResource sr WHERE sr.resourceId = :resourceId AND sr.resourceType = :resourceType AND sr.deleted = false AND sr.status = 'ACTIVE' AND (sr.startDate IS NULL OR sr.startDate <= :now) AND (sr.endDate IS NULL OR sr.endDate > :now) ORDER BY sr.createdAt DESC",
                SpaceResource.class
        );
        query.setParameter("resourceId", resourceId);
        query.setParameter("resourceType", resourceType);
        query.setParameter("now", now);
        return query.getResultList();
    }

    @Override
    public long countBySpaceId(Long spaceId) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(sr) FROM SpaceResource sr WHERE sr.spaceId = :spaceId AND sr.deleted = false",
                Long.class
        );
        query.setParameter("spaceId", spaceId);
        return query.getSingleResult();
    }

    @Override
    @Transactional
    public void deleteBySpaceId(Long spaceId) {
        TypedQuery<SpaceResource> query = entityManager.createQuery(
                "SELECT sr FROM SpaceResource sr WHERE sr.spaceId = :spaceId AND sr.deleted = false",
                SpaceResource.class
        );
        query.setParameter("spaceId", spaceId);
        query.getResultList().forEach(sr -> {
            sr.setDeleted(true);
            sr.setDeletedAt(LocalDateTime.now());
            entityManager.merge(sr);
        });
        log.debug("Soft deleted all space resources for spaceId: {}", spaceId);
    }
}
