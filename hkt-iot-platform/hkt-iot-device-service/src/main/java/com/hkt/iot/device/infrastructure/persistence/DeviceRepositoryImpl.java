package com.hkt.iot.device.infrastructure.persistence;

import com.hkt.iot.domain.repository.OptimisticLockRepository;
import com.hkt.iot.device.domain.model.Device;
import com.hkt.iot.device.domain.repository.DeviceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 设备Repository实现
 * 基于DDL: device表
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
public class DeviceRepositoryImpl implements DeviceRepository, OptimisticLockRepository<Device, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Device save(Device device) {
        if (device.getId() == null) {
            entityManager.persist(device);
            log.debug("Created new device: id={}, sn={}", device.getId(), device.getDeviceSn());
            return device;
        } else {
            Device merged = entityManager.merge(device);
            log.debug("Updated device: id={}, sn={}", merged.getId(), merged.getDeviceSn());
            return merged;
        }
    }

    @Override
    public Optional<Device> findById(Long id) {
        Device device = entityManager.find(Device.class, id);
        if (device != null && device.getDeleted() == 0) {
            return Optional.of(device);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Device> findByTenantIdAndDeviceSn(Long tenantId, String deviceSn) {
        TypedQuery<Device> query = entityManager.createQuery(
            "SELECT d FROM Device d WHERE d.tenantId = :tenantId AND d.deviceSn = :deviceSn AND d.deleted = 0",
            Device.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("deviceSn", deviceSn);
        
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Device> findByTenantId(Long tenantId) {
        TypedQuery<Device> query = entityManager.createQuery(
            "SELECT d FROM Device d WHERE d.tenantId = :tenantId AND d.deleted = 0 ORDER BY d.createdAt DESC",
            Device.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getResultList();
    }

    @Override
    public List<Device> findByTenantIdAndDeviceStatus(Long tenantId, String deviceStatus) {
        TypedQuery<Device> query = entityManager.createQuery(
            "SELECT d FROM Device d WHERE d.tenantId = :tenantId AND d.deviceStatus = :deviceStatus AND d.deleted = 0",
            Device.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("deviceStatus", deviceStatus);
        return query.getResultList();
    }

    @Override
    public List<Device> findByTenantIdAndOnlineStatus(Long tenantId, int onlineStatus) {
        TypedQuery<Device> query = entityManager.createQuery(
            "SELECT d FROM Device d WHERE d.tenantId = :tenantId AND d.onlineStatus = :onlineStatus AND d.deleted = 0",
            Device.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("onlineStatus", onlineStatus);
        return query.getResultList();
    }

    @Override
    public List<Device> findBySpaceId(Long spaceId) {
        TypedQuery<Device> query = entityManager.createQuery(
            "SELECT d FROM Device d WHERE d.spaceId = :spaceId AND d.deleted = 0",
            Device.class
        );
        query.setParameter("spaceId", spaceId);
        return query.getResultList();
    }

    @Override
    public List<Device> findByThingModelId(Long thingModelId) {
        TypedQuery<Device> query = entityManager.createQuery(
            "SELECT d FROM Device d WHERE d.thingModelId = :thingModelId AND d.deleted = 0",
            Device.class
        );
        query.setParameter("thingModelId", thingModelId);
        return query.getResultList();
    }

    @Override
    @Transactional
    public void updateOnlineStatus(Long deviceId, int onlineStatus) {
        Device device = findById(deviceId).orElse(null);
        if (device != null) {
            device.setOnlineStatus(onlineStatus);
            if (onlineStatus == 1) {
                device.setLastOnlineTime(java.time.LocalDateTime.now());
                device.setDeviceStatus("ONLINE");
            } else {
                device.setLastOfflineTime(java.time.LocalDateTime.now());
                device.setDeviceStatus("OFFLINE");
            }
            entityManager.merge(device);
            log.debug("Updated device online status: id={}, status={}", deviceId, onlineStatus);
        }
    }

    @Override
    @Transactional
    public void updateLatestProperties(Long deviceId, String propertiesJson) {
        Device device = findById(deviceId).orElse(null);
        if (device != null) {
            device.setLatestProperties(propertiesJson);
            device.setLastDataTime(java.time.LocalDateTime.now());
            entityManager.merge(device);
            log.debug("Updated device latest properties: id={}", deviceId);
        }
    }

    @Override
    @Transactional
    public void delete(Device device) {
        device.setDeleted(true);
        device.setDeletedAt(java.time.LocalDateTime.now());
        entityManager.merge(device);
        log.debug("Soft deleted device: id={}", device.getId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    public boolean existsByTenantIdAndDeviceSn(Long tenantId, String deviceSn) {
        return findByTenantIdAndDeviceSn(tenantId, deviceSn).isPresent();
    }

    @Override
    public long countByTenantId(Long tenantId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(d) FROM Device d WHERE d.tenantId = :tenantId AND d.deleted = 0",
            Long.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getSingleResult();
    }

    @Override
    public List<Device> findAll() {
        TypedQuery<Device> query = entityManager.createQuery(
            "SELECT d FROM Device d WHERE d.deleted = 0 ORDER BY d.createdAt DESC",
            Device.class
        );
        return query.getResultList();
    }
}
