package com.hkt.iot.user.interfaces.rest;

import com.hkt.iot.user.application.dto.*;
import com.hkt.iot.user.application.dto.MfaDTO.*;
import com.hkt.iot.user.application.dto.CommonDTO;
import com.hkt.iot.user.application.service.MfaApplicationService;
import com.hkt.iot.user.domain.model.MfaConfig;
import com.hkt.iot.common.security.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MFA多因素认证控制器
 *
 * @author HKT IoT Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mfa")
@RequiredArgsConstructor
@Tag(name = "多因素认证管理", description = "MFA多因素认证相关接口")
public class MfaController {

    private final MfaApplicationService mfaApplicationService;

    /**
     * 设置TOTP认证
     */
    @PostMapping("/totp/setup")
    @Operation(summary = "设置TOTP认证", description = "启用TOTP多因素认证，返回密钥和二维码")
    public CommonResponse<MfaSetupResponse> setupTotp() {
        Long userId = SecurityUtil.getCurrentUserId();
        Long tenantId = SecurityUtil.getCurrentTenantId();
        MfaSetupResponse response = mfaApplicationService.setupTotp(userId, tenantId);
        return CommonResponse.success(response);
    }

    /**
     * 验证并启用TOTP
     */
    @PostMapping("/totp/verify")
    @Operation(summary = "验证并启用TOTP", description = "验证TOTP码并正式启用TOTP认证")
    public CommonResponse<Void> verifyAndEnableTotp(@RequestParam String code) {
        Long userId = SecurityUtil.getCurrentUserId();
        mfaApplicationService.verifyAndEnableTotp(userId, code);
        return CommonResponse.success();
    }

    /**
     * 禁用MFA
     */
    @PostMapping("/{mfaType}/disable")
    @Operation(summary = "禁用MFA", description = "禁用指定类型的MFA认证")
    public CommonResponse<Void> disableMfa(@PathVariable String mfaType) {
        Long userId = SecurityUtil.getCurrentUserId();
        MfaConfig.MfaType type = MfaConfig.MfaType.valueOf(mfaType.toUpperCase());
        mfaApplicationService.disableMfa(userId, type);
        return CommonResponse.success();
    }

    /**
     * 获取用户的MFA配置列表
     */
    @GetMapping("/configs")
    @Operation(summary = "获取MFA配置列表", description = "获取当前用户的所有MFA配置")
    public CommonResponse<List<MfaConfigResponse>> getUserMfaConfigs() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<MfaConfigResponse> response = mfaApplicationService.getUserMfaConfigs(userId);
        return CommonResponse.success(response);
    }

    /**
     * 设置主要MFA方式
     */
    @PostMapping("/configs/{configId}/primary")
    @Operation(summary = "设置主要MFA方式", description = "将指定的MFA配置设为主要方式")
    public CommonResponse<Void> setPrimaryMfa(@PathVariable Long configId) {
        Long userId = SecurityUtil.getCurrentUserId();
        mfaApplicationService.setPrimaryMfa(userId, configId);
        return CommonResponse.success();
    }

    /**
     * 注册MFA设备
     */
    @PostMapping("/devices")
    @Operation(summary = "注册MFA设备", description = "注册可信的MFA设备")
    public CommonResponse<Void> registerMfaDevice(@Valid @RequestBody MfaDeviceRegisterRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long tenantId = SecurityUtil.getCurrentTenantId();
        mfaApplicationService.registerMfaDevice(userId, tenantId, request);
        return CommonResponse.success();
    }

    /**
     * 获取用户的MFA设备列表
     */
    @GetMapping("/devices")
    @Operation(summary = "获取MFA设备列表", description = "获取当前用户的所有MFA设备")
    public CommonResponse<List<MfaDeviceResponse>> getUserMfaDevices() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<MfaDeviceResponse> response = mfaApplicationService.getUserMfaDevices(userId);
        return CommonResponse.success(response);
    }

    /**
     * 移除MFA设备
     */
    @DeleteMapping("/devices/{deviceId}")
    @Operation(summary = "移除MFA设备", description = "移除指定的MFA设备")
    public CommonResponse<Void> removeMfaDevice(@PathVariable Long deviceId) {
        Long userId = SecurityUtil.getCurrentUserId();
        mfaApplicationService.removeMfaDevice(userId, deviceId);
        return CommonResponse.success();
    }

    /**
     * 验证MFA码
     */
    @PostMapping("/{mfaType}/verify")
    @Operation(summary = "验证MFA码", description = "验证指定类型的MFA码")
    public CommonResponse<Boolean> verifyMfaCode(
            @PathVariable String mfaType,
            @RequestParam String code) {
        Long userId = SecurityUtil.getCurrentUserId();
        MfaConfig.MfaType type = MfaConfig.MfaType.valueOf(mfaType.toUpperCase());
        boolean valid = mfaApplicationService.verifyMfaCode(userId, code, type);
        return CommonResponse.success(valid);
    }
}
