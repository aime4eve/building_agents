package com.hkt.iot.device.infrastructure.persistence;

import com.hkt.iot.device.domain.model.DeviceThingModel;
import com.hkt.iot.device.domain.repository.DeviceThingModelRepository;
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
 * 设备物模型Repository实现
 * 基于DDL: device_thing_model表
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
public class DeviceThingModelRepositoryImpl implements DeviceThingModelRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public DeviceThingModel save(DeviceThingModel model) {
        if (model.getId() == null) {
            entityManager.persist(model);
            log.debug("Created new device thing model: id={}, deviceModel={}", model.getId(), model.getDeviceModel());
            return model;
        } else {
            DeviceThingModel merged = entityManager.merge(model);
            log.debug("Updated device thing model: id={}, deviceModel={}", merged.getId(), merged.getDeviceModel());
            return merged;
        }
    }

    @Override
    public List<DeviceThingModel> saveAll(List<DeviceThingModel> entities) {
        return entities.stream()
                .map(this::save)
                .toList();
    }

    @Override
    public Optional<DeviceThingModel> findById(Long id) {
        DeviceThingModel model = entityManager.find(DeviceThingModel.class, id);
        if (model != null && !model.getDeleted()) {
            return Optional.of(model);
        }
        return Optional.empty();
    }

    @Override
    public List<DeviceThingModel> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        TypedQuery<DeviceThingModel> query = entityManager.createQuery(
            "SELECT m FROM DeviceThingModel m WHERE m.id IN :ids AND m.deleted = false",
            DeviceThingModel.class
        );
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    @Override
    public List<DeviceThingModel> findAll() {
        TypedQuery<DeviceThingModel> query = entityManager.createQuery(
            "SELECT m FROM DeviceThingModel m WHERE m.deleted = false ORDER BY m.createdAt DESC",
            DeviceThingModel.class
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
            "SELECT COUNT(m) FROM DeviceThingModel m WHERE m.deleted = false",
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
    public void delete(DeviceThingModel model) {
        model.setDeleted(true);
        model.setDeletedAt(LocalDateTime.now());
        entityManager.merge(model);
        log.debug("Soft deleted device thing model: id={}", model.getId());
    }

    @Override
    public void deleteAll(List<DeviceThingModel> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        findAll().forEach(this::delete);
    }

    @Override
    public Optional<DeviceThingModel> findByTenantIdAndDeviceModel(Long tenantId, String deviceModel) {
        TypedQuery<DeviceThingModel> query = entityManager.createQuery(
            "SELECT m FROM DeviceThingModel m WHERE m.tenantId = :tenantId AND m.deviceModel = :deviceModel AND m.deleted = false",
            DeviceThingModel.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("deviceModel", deviceModel);

        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<DeviceThingModel> findByDeviceModel(String deviceModel) {
        TypedQuery<DeviceThingModel> query = entityManager.createQuery(
            "SELECT m FROM DeviceThingModel m WHERE m.deviceModel = :deviceModel AND m.deleted = false",
            DeviceThingModel.class
        );
        query.setParameter("deviceModel", deviceModel);

        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<DeviceThingModel> findByTenantId(Long tenantId) {
        TypedQuery<DeviceThingModel> query = entityManager.createQuery(
            "SELECT m FROM DeviceThingModel m WHERE m.tenantId = :tenantId AND m.deleted = false ORDER BY m.createdAt DESC",
            DeviceThingModel.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getResultList();
    }

    @Override
    public List<DeviceThingModel> findByCategory(String category) {
        TypedQuery<DeviceThingModel> query = entityManager.createQuery(
            "SELECT m FROM DeviceThingModel m WHERE m.category = :category AND m.deleted = false ORDER BY m.createdAt DESC",
            DeviceThingModel.class
        );
        query.setParameter("category", category);
        return query.getResultList();
    }

    @Override
    public List<DeviceThingModel> findByManufacturer(String manufacturer) {
        TypedQuery<DeviceThingModel> query = entityManager.createQuery(
            "SELECT m FROM DeviceThingModel m WHERE m.manufacturer = :manufacturer AND m.deleted = false ORDER BY m.createdAt DESC",
            DeviceThingModel.class
        );
        query.setParameter("manufacturer", manufacturer);
        return query.getResultList();
    }

    @Override
    public List<DeviceThingModel> findByStatus(Integer status) {
        TypedQuery<DeviceThingModel> query = entityManager.createQuery(
            "SELECT m FROM DeviceThingModel m WHERE m.status = :status AND m.deleted = false ORDER BY m.createdAt DESC",
            DeviceThingModel.class
        );
        query.setParameter("status", status);
        return query.getResultList();
    }

    @Override
    public long countByTenantId(Long tenantId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(m) FROM DeviceThingModel m WHERE m.tenantId = :tenantId AND m.deleted = false",
            Long.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getSingleResult();
    }

    @Override
    public boolean existsByTenantIdAndDeviceModel(Long tenantId, String deviceModel) {
        return findByTenantIdAndDeviceModel(tenantId, deviceModel).isPresent();
    }
}
