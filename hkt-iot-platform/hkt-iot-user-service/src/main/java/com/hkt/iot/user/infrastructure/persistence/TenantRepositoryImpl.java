package com.hkt.iot.user.infrastructure.persistence;

import com.hkt.iot.domain.repository.OptimisticLockRepository;
import com.hkt.iot.user.domain.model.Tenant;
import com.hkt.iot.user.domain.repository.TenantRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 租户Repository实现
 * 基于DDL: tenant表
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
public class TenantRepositoryImpl implements TenantRepository, OptimisticLockRepository<Tenant, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Tenant save(Tenant tenant) {
        if (tenant.getId() == null) {
            entityManager.persist(tenant);
            log.debug("Created new tenant: id={}, code={}", tenant.getId(), tenant.getTenantCode());
            return tenant;
        } else {
            Tenant merged = entityManager.merge(tenant);
            log.debug("Updated tenant: id={}, code={}", merged.getId(), merged.getTenantCode());
            return merged;
        }
    }

    @Override
    public Optional<Tenant> findById(Long id) {
        Tenant tenant = entityManager.find(Tenant.class, id);
        if (tenant != null && tenant.getDeleted() == 0) {
            return Optional.of(tenant);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Tenant> findByTenantCode(String tenantCode) {
        TypedQuery<Tenant> query = entityManager.createQuery(
            "SELECT t FROM Tenant t WHERE t.tenantCode = :tenantCode AND t.deleted = 0",
            Tenant.class
        );
        query.setParameter("tenantCode", tenantCode);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Tenant> findByStatus(String status) {
        TypedQuery<Tenant> query = entityManager.createQuery(
            "SELECT t FROM Tenant t WHERE t.tenantStatus = :status AND t.deleted = 0 ORDER BY t.createdAt DESC",
            Tenant.class
        );
        query.setParameter("status", status);
        return query.getResultList();
    }

    @Override
    public List<Tenant> findAll() {
        TypedQuery<Tenant> query = entityManager.createQuery(
            "SELECT t FROM Tenant t WHERE t.deleted = 0 ORDER BY t.createdAt DESC",
            Tenant.class
        );
        return query.getResultList();
    }

    @Override
    @Transactional
    public void delete(Tenant tenant) {
        tenant.setDeleted(true);
        tenant.setDeletedAt(java.time.LocalDateTime.now());
        entityManager.merge(tenant);
        log.debug("Soft deleted tenant: id={}", tenant.getId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    public boolean existsByTenantCode(String tenantCode) {
        return findByTenantCode(tenantCode).isPresent();
    }

    @Override
    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(t) FROM Tenant t WHERE t.deleted = 0",
            Long.class
        );
        return query.getSingleResult();
    }
}
