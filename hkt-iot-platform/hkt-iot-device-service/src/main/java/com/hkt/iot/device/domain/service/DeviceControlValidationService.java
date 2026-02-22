package com.hkt.iot.device.domain.service;

import com.hkt.iot.device.domain.model.Device;
import com.hkt.iot.device.domain.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 设备控制权限校验服务
 * 负责验证设备控制请求的权限和状态
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceControlValidationService {

    private final DeviceRepository deviceRepository;

    /**
     * 验证设备控制权限
     *
     * @param tenantId 租户ID
     * @param deviceId 设备ID
     * @param userId   用户ID
     * @param permissionCode 权限编码
     * @return 验证结果
     */
    public ValidationResult validateControlPermission(Long tenantId, Long deviceId, 
                                                       Long userId, String permissionCode) {
        Map<String, String> errors = new HashMap<>();

        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            return ValidationResult.failure("DEVICE_NOT_FOUND", "设备不存在: " + deviceId);
        }

        Device device = deviceOpt.get();

        if (!device.getTenantId().equals(tenantId)) {
            return ValidationResult.failure("TENANT_MISMATCH", "设备不属于当前租户");
        }

        if (!device.getOnlineStatus()) {
            errors.put("onlineStatus", "设备离线，无法发送控制命令");
        }

        if (device.getIsLocked()) {
            errors.put("isLocked", "设备已被锁定，无法发送控制命令。锁定原因: " + device.getLockReason());
        }

        if (device.getActivationStatus() != Device.ActivationStatus.ACTIVE) {
            errors.put("activationStatus", "设备未激活，无法发送控制命令");
        }

        if (device.getDeviceStatus() == Device.DeviceStatus.FAULT) {
            errors.put("deviceStatus", "设备故障中，无法发送控制命令");
        }

        if (device.getDeviceStatus() == Device.DeviceStatus.MAINTENANCE) {
            errors.put("deviceStatus", "设备维护中，无法发送控制命令");
        }

        if (device.getDeleted()) {
            errors.put("deleted", "设备已删除，无法发送控制命令");
        }

        if (!errors.isEmpty()) {
            return ValidationResult.failure("VALIDATION_FAILED", "设备控制验证失败", errors);
        }

        log.debug("设备控制权限验证通过: deviceId={}, tenantId={}, userId={}", deviceId, tenantId, userId);
        return ValidationResult.success();
    }

    /**
     * 验证批量控制权限
     *
     * @param tenantId 租户ID
     * @param deviceIds 设备ID列表
     * @param userId 用户ID
     * @return 批量验证结果
     */
    public BatchValidationResult validateBatchControl(Long tenantId, java.util.List<Long> deviceIds, 
                                                       Long userId) {
        Map<Long, ValidationResult> results = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;

        for (Long deviceId : deviceIds) {
            ValidationResult result = validateControlPermission(tenantId, deviceId, userId, "device:control");
            results.put(deviceId, result);
            if (result.isValid()) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        return new BatchValidationResult(results, successCount, failureCount);
    }

    /**
     * 验证控制时间窗口
     *
     * @param deviceId 设备ID
     * @param allowedStartTime 允许开始时间 (HH:mm)
     * @param allowedEndTime 允许结束时间 (HH:mm)
     * @return 验证结果
     */
    public ValidationResult validateControlTimeWindow(Long deviceId, String allowedStartTime, 
                                                       String allowedEndTime) {
        if (allowedStartTime == null || allowedEndTime == null) {
            return ValidationResult.success();
        }

        try {
            LocalTime now = LocalTime.now();
            LocalTime start = LocalTime.parse(allowedStartTime);
            LocalTime end = LocalTime.parse(allowedEndTime);

            boolean isWithinWindow;
            if (start.isBefore(end)) {
                isWithinWindow = !now.isBefore(start) && !now.isAfter(end);
            } else {
                isWithinWindow = !now.isBefore(start) || !now.isAfter(end);
            }

            if (!isWithinWindow) {
                return ValidationResult.failure("TIME_WINDOW_VIOLATION", 
                        String.format("当前时间不在允许控制的时间窗口内。允许时间: %s - %s", 
                                allowedStartTime, allowedEndTime));
            }

            return ValidationResult.success();
        } catch (Exception e) {
            log.error("解析时间窗口失败: {} - {}", allowedStartTime, allowedEndTime, e);
            return ValidationResult.failure("TIME_WINDOW_PARSE_ERROR", "时间窗口配置格式错误");
        }
    }

    /**
     * 验证设备命令参数
     *
     * @param deviceId 设备ID
     * @param commandCode 命令编码
     * @param params 命令参数
     * @return 验证结果
     */
    public ValidationResult validateCommandParams(Long deviceId, String commandCode, 
                                                   Map<String, Object> params) {
        if (commandCode == null || commandCode.isEmpty()) {
            return ValidationResult.failure("COMMAND_CODE_EMPTY", "命令编码不能为空");
        }

        return ValidationResult.success();
    }

    /**
     * 验证结果
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ValidationResult {
        private boolean valid;
        private String errorCode;
        private String errorMessage;
        private Map<String, String> details;

        public static ValidationResult success() {
            return new ValidationResult(true, null, null, null);
        }

        public static ValidationResult failure(String errorCode, String errorMessage) {
            return new ValidationResult(false, errorCode, errorMessage, null);
        }

        public static ValidationResult failure(String errorCode, String errorMessage, 
                                                Map<String, String> details) {
            return new ValidationResult(false, errorCode, errorMessage, details);
        }
    }

    /**
     * 批量验证结果
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class BatchValidationResult {
        private Map<Long, ValidationResult> results;
        private int successCount;
        private int failureCount;

        public boolean isAllSuccess() {
            return failureCount == 0;
        }

        public java.util.List<Long> getValidDeviceIds() {
            return results.entrySet().stream()
                    .filter(e -> e.getValue().isValid())
                    .map(Map.Entry::getKey)
                    .collect(java.util.stream.Collectors.toList());
        }

        public java.util.List<Long> getInvalidDeviceIds() {
            return results.entrySet().stream()
                    .filter(e -> !e.getValue().isValid())
                    .map(Map.Entry::getKey)
                    .collect(java.util.stream.Collectors.toList());
        }
    }
}
