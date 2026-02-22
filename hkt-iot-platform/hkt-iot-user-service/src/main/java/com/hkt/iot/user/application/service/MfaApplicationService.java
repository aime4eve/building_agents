package com.hkt.iot.user.application.service;

import com.hkt.iot.common.core.exception.BizException;
import com.hkt.iot.common.core.result.ResultCode;
import com.hkt.iot.common.security.totp.TotpUtil;
import com.hkt.iot.user.domain.model.MfaConfig;
import com.hkt.iot.user.domain.model.MfaDevice;
import com.hkt.iot.user.domain.model.User;
import com.hkt.iot.user.domain.repository.MfaConfigRepository;
import com.hkt.iot.user.domain.repository.MfaDeviceRepository;
import com.hkt.iot.user.domain.repository.UserRepository;
import com.hkt.iot.user.application.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MFA多因素认证应用服务
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MfaApplicationService {

    private final MfaConfigRepository mfaConfigRepository;
    private final MfaDeviceRepository mfaDeviceRepository;
    private final UserRepository userRepository;

    /**
     * 启用TOTP认证
     */
    @Transactional
    public MfaSetupResponse setupTotp(Long userId, Long tenantId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        // 检查是否已启用TOTP
        MfaConfig existingConfig = mfaConfigRepository.findByUserIdAndMfaType(userId, MfaConfig.MfaType.TOTP);
        if (existingConfig != null && existingConfig.isConfigured()) {
            throw new BizException(ResultCode.MFA_ALREADY_ENABLED);
        }

        // 生成密钥
        String secret = TotpUtil.generateSecret();

        // 创建或更新配置
        MfaConfig config;
        if (existingConfig == null) {
            config = MfaConfig.create(userId, tenantId, MfaConfig.MfaType.TOTP, secret);
        } else {
            config = existingConfig;
            config.updateSecretKey(secret);
        }

        // 生成备用恢复码
        List<String> backupCodes = generateBackupCodes();
        config.generateBackupCodes(backupCodes);

        mfaConfigRepository.save(config);

        // 生成QR码URL
        String qrCodeUrl = TotpUtil.getQrCodeUrl(user.getUsername(), secret, "HKT-IoT-Platform");

        log.info("设置TOTP MFA: userId={}", userId);

        return MfaSetupResponse.builder()
                .secret(secret)
                .qrCodeUrl(qrCodeUrl)
                .backupCodes(backupCodes)
                .message("请使用认证器应用扫描二维码")
                .build();
    }

    /**
     * 验证并启用TOTP
     */
    @Transactional
    public void verifyAndEnableTotp(Long userId, String code) {
        MfaConfig config = mfaConfigRepository.findByUserIdAndMfaType(userId, MfaConfig.MfaType.TOTP);
        if (config == null) {
            throw new BizException(ResultCode.MFA_NOT_SETUP);
        }

        // 验证TOTP码
        if (!TotpUtil.verifyCode(config.getSecretKey(), code)) {
            throw new BizException(ResultCode.MFA_CODE_INVALID);
        }

        // 启用MFA
        config.enable();
        config.setAsPrimary();
        mfaConfigRepository.save(config);

        // 更新用户MFA状态
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));
        user.enableMfa(config.getSecretKey());
        userRepository.save(user);

        log.info("启用TOTP MFA成功: userId={}", userId);
    }

    /**
     * 禁用MFA
     */
    @Transactional
    public void disableMfa(Long userId, MfaConfig.MfaType mfaType) {
        MfaConfig config = mfaConfigRepository.findByUserIdAndMfaType(userId, mfaType);
        if (config == null) {
            throw new BizException(ResultCode.MFA_NOT_ENABLED);
        }

        config.disable();
        mfaConfigRepository.save(config);

        // 检查是否还有其他启用的MFA方式
        List<MfaConfig> allConfigs = mfaConfigRepository.findByUserId(userId);
        boolean hasEnabledMfa = allConfigs.stream()
                .anyMatch(MfaConfig::isConfigured);

        // 更新用户MFA状态
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));
        if (!hasEnabledMfa) {
            user.disableMfa();
        }
        userRepository.save(user);

        log.info("禁用MFA成功: userId={}, type={}", userId, mfaType);
    }

    /**
     * 获取用户的MFA配置列表
     */
    public List<MfaConfigResponse> getUserMfaConfigs(Long userId) {
        List<MfaConfig> configs = mfaConfigRepository.findByUserId(userId);

        return configs.stream()
                .map(this::toMfaConfigResponse)
                .collect(Collectors.toList());
    }

    /**
     * 设置主要MFA方式
     */
    @Transactional
    public void setPrimaryMfa(Long userId, Long configId) {
        List<MfaConfig> configs = mfaConfigRepository.findByUserId(userId);

        // 取消所有主要设置
        configs.forEach(c -> {
            if (c.getId().equals(configId)) {
                c.setAsPrimary();
            } else {
                c.setIsPrimary(false);
            }
            mfaConfigRepository.save(c);
        });

        log.info("设置主要MFA方式: userId={}, configId={}", userId, configId);
    }

    /**
     * 注册MFA设备
     */
    @Transactional
    public void registerMfaDevice(Long userId, Long tenantId, MfaDeviceRegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        MfaDevice device = MfaDevice.create(
                userId,
                tenantId,
                request.getDeviceType(),
                request.getDeviceName(),
                request.getDeviceIdentifier(),
                request.getDeviceInfo()
        );

        mfaDeviceRepository.save(device);

        log.info("注册MFA设备: userId={}, deviceType={}", userId, request.getDeviceType());
    }

    /**
     * 获取用户的MFA设备列表
     */
    public List<MfaDeviceResponse> getUserMfaDevices(Long userId) {
        List<MfaDevice> devices = mfaDeviceRepository.findByUserId(userId);

        return devices.stream()
                .map(this::toMfaDeviceResponse)
                .collect(Collectors.toList());
    }

    /**
     * 移除MFA设备
     */
    @Transactional
    public void removeMfaDevice(Long userId, Long deviceId) {
        MfaDevice device = mfaDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new BizException(ResultCode.MFA_DEVICE_NOT_EXIST));

        if (!device.getUserId().equals(userId)) {
            throw new BizException(ResultCode.MFA_DEVICE_NOT_BELONG_TO_USER);
        }

        device.revoke();
        mfaDeviceRepository.save(device);

        log.info("移除MFA设备: userId={}, deviceId={}", userId, deviceId);
    }

    /**
     * 验证MFA码
     */
    public boolean verifyMfaCode(Long userId, String code, MfaConfig.MfaType mfaType) {
        MfaConfig config = mfaConfigRepository.findByUserIdAndMfaType(userId, mfaType);
        if (config == null || !config.isConfigured()) {
            return false;
        }

        switch (mfaType) {
            case TOTP:
                return TotpUtil.verifyCode(config.getSecretKey(), code);
            case SMS:
            case EMAIL:
            case HARDWARE_TOKEN:
                // TODO: 实现其他类型的验证
                return false;
            default:
                return false;
        }
    }

    /**
     * 生成备用恢复码
     */
    private List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            codes.add(UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        }
        return codes;
    }

    /**
     * 转换为响应对象
     */
    private MfaConfigResponse toMfaConfigResponse(MfaConfig config) {
        return MfaConfigResponse.builder()
                .id(config.getId())
                .userId(config.getUserId())
                .mfaType(config.getMfaType().name())
                .isEnabled(config.getIsEnabled())
                .isPrimary(config.getIsPrimary())
                .status(config.getStatus().name())
                .createdAt(config.getCreatedAt())
                .build();
    }

    private MfaDeviceResponse toMfaDeviceResponse(MfaDevice device) {
        return MfaDeviceResponse.builder()
                .id(device.getId())
                .userId(device.getUserId())
                .deviceType(device.getDeviceType())
                .deviceName(device.getDeviceName())
                .deviceIdentifier(device.getDeviceIdentifier())
                .isTrusted(device.getIsTrusted())
                .lastUsedAt(device.getLastUsedAt())
                .status(device.getStatus().name())
                .createdAt(device.getCreatedAt())
                .build();
    }
}
