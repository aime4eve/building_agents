package com.hkt.iot.device.application.service;

import com.hkt.iot.device.domain.event.DeviceStatusChangedEvent;
import com.hkt.iot.device.domain.model.Device;
import com.hkt.iot.device.domain.repository.DeviceRepository;
import com.hkt.iot.device.domain.repository.DeviceThingModelRepository;
import com.hkt.iot.device.interfaces.rest.dto.DeviceCreateDTO;
import com.hkt.iot.device.interfaces.rest.dto.DeviceUpdateDTO;
import com.hkt.iot.device.application.event.DeviceEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 设备应用服务
 * 负责设备管理的应用层业务逻辑
 *
 * @author HKT IoT Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceApplicationService {

    private final DeviceRepository deviceRepository;
    private final DeviceThingModelRepository thingModelRepository;
    private final DeviceEventPublisher eventPublisher;

    /**
     * 创建设备
     */
    @Transactional(rollbackFor = Exception.class)
    public Device createDevice(DeviceCreateDTO dto, Long createdBy) {
        log.info("创建设备: tenantId={}, deviceSn={}, deviceName={}",
                dto.getTenantId(), dto.getDeviceSn(), dto.getDeviceName());

        // 检查设备序列号是否已存在
        if (deviceRepository.existsByTenantIdAndDeviceSn(dto.getTenantId(), dto.getDeviceSn())) {
            throw new IllegalArgumentException("设备序列号已存在: " + dto.getDeviceSn());
        }

        // 验证物模型是否存在
        if (dto.getThingModelId() != null) {
            thingModelRepository.findById(dto.getThingModelId())
                    .orElseThrow(() -> new IllegalArgumentException("物模型不存在: " + dto.getThingModelId()));
        }

        // 创建设备
        Device device = Device.create(
                dto.getTenantId(),
                dto.getDeviceSn(),
                dto.getDeviceName(),
                dto.getDeviceType(),
                dto.getDeviceModel(),
                dto.getDeviceCategory(),
                createdBy
        );

        // 设置扩展属性
        if (dto.getDeviceCode() != null) {
            device.setDeviceCode(dto.getDeviceCode());
        }
        if (dto.getThingModelId() != null) {
            device.setThingModelId(dto.getThingModelId());
        }
        if (dto.getSpaceId() != null) {
            device.setSpaceId(dto.getSpaceId());
        }
        if (dto.getParentDeviceId() != null) {
            device.setParentDeviceId(dto.getParentDeviceId());
        }
        if (dto.getLocationDesc() != null) {
            device.setLocationDesc(dto.getLocationDesc());
        }
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            device.setLongitude(dto.getLongitude());
            device.setLatitude(dto.getLatitude());
            device.setAltitude(dto.getAltitude());
        }
        if (dto.getFirmwareVersion() != null) {
            device.setFirmwareVersion(dto.getFirmwareVersion());
        }
        if (dto.getHardwareVersion() != null) {
            device.setHardwareVersion(dto.getHardwareVersion());
        }
        if (dto.getSoftwareVersion() != null) {
            device.setSoftwareVersion(dto.getSoftwareVersion());
        }

        // 保存设备
        Device savedDevice = deviceRepository.save(device);

        // 发布领域事件
        eventPublisher.publishDomainEvents(device);

        log.info("设备创建成功: deviceId={}, deviceSn={}", savedDevice.getId(), savedDevice.getDeviceSn());
        return savedDevice;
    }

    /**
     * 更新设备
     */
    @Transactional(rollbackFor = Exception.class)
    public Device updateDevice(Long deviceId, DeviceUpdateDTO dto, Long updatedBy) {
        log.info("更新设备: deviceId={}", deviceId);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));

        Device.DeviceStatus oldStatus = device.getDeviceStatus();

        // 更新设备信息
        if (dto.getDeviceName() != null) {
            device.setDeviceName(dto.getDeviceName());
        }
        if (dto.getSpaceId() != null) {
            device.setSpaceId(dto.getSpaceId());
        }
        if (dto.getLocationDesc() != null) {
            device.setLocationDesc(dto.getLocationDesc());
        }
        if (dto.getLongitude() != null && dto.getLatitude() != null) {
            device.setLongitude(dto.getLongitude());
            device.setLatitude(dto.getLatitude());
            device.setAltitude(dto.getAltitude());
        }
        if (dto.getFirmwareVersion() != null) {
            device.setFirmwareVersion(dto.getFirmwareVersion());
        }
        if (dto.getHardwareVersion() != null) {
            device.setHardwareVersion(dto.getHardwareVersion());
        }
        if (dto.getSoftwareVersion() != null) {
            device.setSoftwareVersion(dto.getSoftwareVersion());
        }

        device.setUpdatedBy(updatedBy);

        // 保存更新
        Device savedDevice = deviceRepository.save(device);

        // 如果状态变更，发布状态变更事件
        if (dto.getDeviceStatus() != null && dto.getDeviceStatus() != oldStatus) {
            device.setDeviceStatus(dto.getDeviceStatus());
            DeviceStatusChangedEvent event = new DeviceStatusChangedEvent(
                    device.getId(),
                    device.getDeviceSn(),
                    device.getTenantId(),
                    oldStatus,
                    dto.getDeviceStatus(),
                    device.getOnlineStatus(),
                    device.getUpdatedAt()
            );
            eventPublisher.publishEvent(event);
        }

        log.info("设备更新成功: deviceId={}", deviceId);
        return savedDevice;
    }

    /**
     * 删除设备
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDevice(Long deviceId, Long deletedBy) {
        log.info("删除设备: deviceId={}", deviceId);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));

        device.softDelete(deletedBy);
        deviceRepository.save(device);

        log.info("设备删除成功: deviceId={}", deviceId);
    }

    /**
     * 激活设备
     */
    @Transactional(rollbackFor = Exception.class)
    public void activateDevice(Long deviceId) {
        log.info("激活设备: deviceId={}", deviceId);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));

        device.activate();
        deviceRepository.save(device);

        log.info("设备激活成功: deviceId={}", deviceId);
    }

    /**
     * 停用设备
     */
    @Transactional(rollbackFor = Exception.class)
    public void deactivateDevice(Long deviceId) {
        log.info("停用设备: deviceId={}", deviceId);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));

        device.deactivate();
        deviceRepository.save(device);

        log.info("设备停用成功: deviceId={}", deviceId);
    }

    /**
     * 锁定设备
     */
    @Transactional(rollbackFor = Exception.class)
    public void lockDevice(Long deviceId, Long lockedBy, String reason) {
        log.info("锁定设备: deviceId={}, reason={}", deviceId, reason);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));

        device.lock(lockedBy, reason);
        deviceRepository.save(device);

        log.info("设备锁定成功: deviceId={}", deviceId);
    }

    /**
     * 解锁设备
     */
    @Transactional(rollbackFor = Exception.class)
    public void unlockDevice(Long deviceId) {
        log.info("解锁设备: deviceId={}", deviceId);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));

        device.unlock();
        deviceRepository.save(device);

        log.info("设备解锁成功: deviceId={}", deviceId);
    }

    /**
     * 设备上线
     */
    @Transactional(rollbackFor = Exception.class)
    public void deviceOnline(Long deviceId, String ipAddress) {
        log.info("设备上线: deviceId={}, ipAddress={}", deviceId, ipAddress);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));

        Device.DeviceStatus oldStatus = device.getDeviceStatus();

        device.goOnline(ipAddress);
        deviceRepository.save(device);

        // 发布状态变更事件
        DeviceStatusChangedEvent event = new DeviceStatusChangedEvent(
                device.getId(),
                device.getDeviceSn(),
                device.getTenantId(),
                oldStatus,
                device.getDeviceStatus(),
                device.getOnlineStatus(),
                device.getLastOnlineTime()
        );
        eventPublisher.publishEvent(event);

        log.info("设备上线成功: deviceId={}", deviceId);
    }

    /**
     * 设备离线
     */
    @Transactional(rollbackFor = Exception.class)
    public void deviceOffline(Long deviceId) {
        log.info("设备离线: deviceId={}", deviceId);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));

        Device.DeviceStatus oldStatus = device.getDeviceStatus();

        device.goOffline();
        deviceRepository.save(device);

        // 发布状态变更事件
        DeviceStatusChangedEvent event = new DeviceStatusChangedEvent(
                device.getId(),
                device.getDeviceSn(),
                device.getTenantId(),
                oldStatus,
                device.getDeviceStatus(),
                device.getOnlineStatus(),
                device.getLastOfflineTime()
        );
        eventPublisher.publishEvent(event);

        log.info("设备离线成功: deviceId={}", deviceId);
    }

    /**
     * 查询设备详情
     */
    @Transactional(readOnly = true)
    public Device getDeviceById(Long deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));
    }

    /**
     * 根据设备序列号查询设备
     */
    @Transactional(readOnly = true)
    public Optional<Device> getDeviceBySn(Long tenantId, String deviceSn) {
        return deviceRepository.findByTenantIdAndDeviceSn(tenantId, deviceSn);
    }

    /**
     * 查询租户下的所有设备
     */
    @Transactional(readOnly = true)
    public List<Device> getDevicesByTenantId(Long tenantId) {
        return deviceRepository.findByTenantIdAndDeviceStatus(tenantId, null);
    }

    /**
     * 根据空间ID查询设备
     */
    @Transactional(readOnly = true)
    public List<Device> getDevicesBySpaceId(Long spaceId) {
        return deviceRepository.findBySpaceId(spaceId);
    }

    /**
     * 统计租户下的设备数量
     */
    @Transactional(readOnly = true)
    public long countDevicesByTenantId(Long tenantId) {
        return deviceRepository.countByTenantId(tenantId);
    }
}
