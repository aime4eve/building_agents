package com.hkt.iot.device.infrastructure.persistence;

import com.hkt.iot.device.domain.model.DeviceLicense;
import com.hkt.iot.device.domain.model.DeviceLicense.LicenseStatus;
import com.hkt.iot.device.domain.model.DeviceLicense.LicenseType;
import com.hkt.iot.device.domain.repository.DeviceLicenseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 设备License Repository实现
 * 基于DDL: device_license表
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
public class DeviceLicenseRepositoryImpl implements DeviceLicenseRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public DeviceLicense save(DeviceLicense license) {
        if (license.getId() == null) {
            entityManager.persist(license);
            log.debug("Created new device license: id={}, licenseKey={}", license.getId(), license.getLicenseKey());
            return license;
        } else {
            DeviceLicense merged = entityManager.merge(license);
            log.debug("Updated device license: id={}, licenseKey={}", merged.getId(), merged.getLicenseKey());
            return merged;
        }
    }

    @Override
    public List<DeviceLicense> saveAll(List<DeviceLicense> entities) {
        return entities.stream()
                .map(this::save)
                .toList();
    }

    @Override
    public Optional<DeviceLicense> findById(Long id) {
        DeviceLicense license = entityManager.find(DeviceLicense.class, id);
        if (license != null && !license.getDeleted()) {
            return Optional.of(license);
        }
        return Optional.empty();
    }

    @Override
    public List<DeviceLicense> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        TypedQuery<DeviceLicense> query = entityManager.createQuery(
            "SELECT l FROM DeviceLicense l WHERE l.id IN :ids AND l.deleted = false",
            DeviceLicense.class
        );
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    @Override
    public List<DeviceLicense> findAll() {
        TypedQuery<DeviceLicense> query = entityManager.createQuery(
            "SELECT l FROM DeviceLicense l WHERE l.deleted = false ORDER BY l.createdAt DESC",
            DeviceLicense.class
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
            "SELECT COUNT(l) FROM DeviceLicense l WHERE l.deleted = false",
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
    public void delete(DeviceLicense license) {
        license.setDeleted(true);
        license.setDeletedAt(LocalDateTime.now());
        entityManager.merge(license);
        log.debug("Soft deleted device license: id={}", license.getId());
    }

    @Override
    public void deleteAll(List<DeviceLicense> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        findAll().forEach(this::delete);
    }

    @Override
    public Optional<DeviceLicense> findByTenantIdAndLicenseKey(Long tenantId, String licenseKey) {
        TypedQuery<DeviceLicense> query = entityManager.createQuery(
            "SELECT l FROM DeviceLicense l WHERE l.tenantId = :tenantId AND l.licenseKey = :licenseKey AND l.deleted = false",
            DeviceLicense.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("licenseKey", licenseKey);

        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<DeviceLicense> findByLicenseKey(String licenseKey) {
        TypedQuery<DeviceLicense> query = entityManager.createQuery(
            "SELECT l FROM DeviceLicense l WHERE l.licenseKey = :licenseKey AND l.deleted = false",
            DeviceLicense.class
        );
        query.setParameter("licenseKey", licenseKey);

        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<DeviceLicense> findByDeviceId(Long deviceId) {
        TypedQuery<DeviceLicense> query = entityManager.createQuery(
            "SELECT l FROM DeviceLicense l WHERE l.deviceId = :deviceId AND l.deleted = false ORDER BY l.createdAt DESC",
            DeviceLicense.class
        );
        query.setParameter("deviceId", deviceId);
        return query.getResultList();
    }

    @Override
    public List<DeviceLicense> findByTenantIdAndDeviceId(Long tenantId, Long deviceId) {
        TypedQuery<DeviceLicense> query = entityManager.createQuery(
            "SELECT l FROM DeviceLicense l WHERE l.tenantId = :tenantId AND l.deviceId = :deviceId AND l.deleted = false ORDER BY l.createdAt DESC",
            DeviceLicense.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("deviceId", deviceId);
        return query.getResultList();
    }

    @Override
    public List<DeviceLicense> findByDeviceSn(String deviceSn) {
        TypedQuery<DeviceLicense> query = entityManager.createQuery(
            "SELECT l FROM DeviceLicense l WHERE l.deviceSn = :deviceSn AND l.deleted = false ORDER BY l.createdAt DESC",
            DeviceLicense.class
        );
        query.setParameter("deviceSn", deviceSn);
        return query.getResultList();
    }

    @Override
    public List<DeviceLicense> findByTenantId(Long tenantId) {
        TypedQuery<DeviceLicense> query = entityManager.createQuery(
            "SELECT l FROM DeviceLicense l WHERE l.tenantId = :tenantId AND l.deleted = false ORDER BY l.createdAt DESC",
            DeviceLicense.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getResultList();
    }

    @Override
    public List<DeviceLicense> findByTenantIdAndLicenseType(Long tenantId, LicenseType licenseType) {
        TypedQuery<DeviceLicense> query = entityManager.createQuery(
            "SELECT l FROM DeviceLicense l WHERE l.tenantId = :tenantId AND l.licenseType = :licenseType AND l.deleted = false ORDER BY l.createdAt DESC",
            DeviceLicense.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("licenseType", licenseType);
        return query.getResultList();
    }

    @Override
    public List<DeviceLicense> findByTenantIdAndLicenseStatus(Long tenantId, LicenseStatus licenseStatus) {
        TypedQuery<DeviceLicense> query = entityManager.createQuery(
            "SELECT l FROM DeviceLicense l WHERE l.tenantId = :tenantId AND l.licenseStatus = :licenseStatus AND l.deleted = false ORDER BY l.createdAt DESC",
            DeviceLicense.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("licenseStatus", licenseStatus);
        return query.getResultList();
    }

    @Override
    public List<DeviceLicense> findByTenantIdAndEndDateBefore(Long tenantId, LocalDate beforeDate) {
        TypedQuery<DeviceLicense> query = entityManager.createQuery(
            "SELECT l FROM DeviceLicense l WHERE l.tenantId = :tenantId AND l.endDate < :beforeDate AND l.deleted = false ORDER BY l.endDate ASC",
            DeviceLicense.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("beforeDate", beforeDate);
        return query.getResultList();
    }

    @Override
    public List<DeviceLicense> findExpiredByTenantId(Long tenantId) {
        return findByTenantIdAndEndDateBefore(tenantId, LocalDate.now());
    }

    @Override
    public long countByTenantId(Long tenantId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(l) FROM DeviceLicense l WHERE l.tenantId = :tenantId AND l.deleted = false",
            Long.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getSingleResult();
    }

    @Override
    public boolean existsByLicenseKey(String licenseKey) {
        return findByLicenseKey(licenseKey).isPresent();
    }

    @Override
    public boolean existsByTenantIdAndLicenseKey(Long tenantId, String licenseKey) {
        return findByTenantIdAndLicenseKey(tenantId, licenseKey).isPresent();
    }
}
