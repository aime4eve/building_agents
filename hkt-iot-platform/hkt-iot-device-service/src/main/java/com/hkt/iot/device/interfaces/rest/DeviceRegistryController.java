package com.hkt.iot.device.interfaces.rest;

import com.hkt.iot.common.result.Result;
import com.hkt.iot.device.application.command.DeviceRegisterCommand;
import com.hkt.iot.device.application.service.DeviceRegistryApplicationService;
import com.hkt.iot.device.interfaces.dto.DeviceRegistryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 设备注册控制器
 * 处理设备注册、证书管理相关接口
 *
 * @author HKT IoT Team
 */
@Slf4j
@Tag(name = "设备注册", description = "设备注册、证书管理接口")
@RestController
@RequestMapping("/api/v1/device-registry")
@RequiredArgsConstructor
public class DeviceRegistryController {

    private final DeviceRegistryApplicationService deviceRegistryApplicationService;

    /**
     * 设备注册
     * 设备首次使用前需要在平台注册，获取设备证书和连接配置
     */
    @Operation(summary = "设备注册", description = "设备首次使用前注册，获取设备证书和MQTT连接配置")
    @PostMapping("/register")
    public Result<DeviceRegistryDTO> register(@Valid @RequestBody DeviceRegisterCommand command) {
        log.info("设备注册请求: deviceSn={}, deviceType={}, tenantId={}",
                command.getDeviceSn(), command.getDeviceType(), command.getTenantId());

        DeviceRegistryDTO result = deviceRegistryApplicationService.registerDevice(command);
        return Result.success(result);
    }

    /**
     * 证书续期
     * 设备证书即将过期时，申请续期
     */
    @Operation(summary = "证书续期", description = "设备证书即将过期时申请续期")
    @PostMapping("/renew-cert")
    public Result<DeviceRegistryDTO> renewCert(@RequestBody CertificateRenewCommand command) {
        log.info("证书续期请求: deviceId={}", command.getDeviceId());

        DeviceRegistryDTO result = deviceRegistryApplicationService.renewCertificate(command);
        return Result.success(result);
    }

    /**
     * 查询设备注册信息
     */
    @Operation(summary = "查询设备注册信息", description = "查询设备的注册信息和连接配置")
    @GetMapping("/{deviceId}")
    public Result<DeviceRegistryDTO> getDeviceInfo(@PathVariable String deviceId) {
        log.info("查询设备注册信息: deviceId={}", deviceId);

        DeviceRegistryDTO result = deviceRegistryApplicationService.getDeviceInfo(deviceId);
        return Result.success(result);
    }

    /**
     * Token刷新
     * JWT Token即将过期时，刷新Token
     */
    @Operation(summary = "Token刷新", description = "JWT Token即将过期时刷新Token")
    @PostMapping("/{deviceId}/refresh-token")
    public Result<TokenRefreshDTO> refreshToken(@PathVariable String deviceId) {
        log.info("Token刷新请求: deviceId={}", deviceId);

        TokenRefreshDTO result = deviceRegistryApplicationService.refreshToken(deviceId);
        return Result.success(result);
    }

    /**
     * 证书续期命令
     */
    public static class CertificateRenewCommand {
        private String deviceId;
        private String oldCertSn;

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getOldCertSn() { return oldCertSn; }
        public void setOldCertSn(String oldCertSn) { this.oldCertSn = oldCertSn; }
    }

    /**
     * Token刷新响应
     */
    public static class TokenRefreshDTO {
        private String token;
        private Long tokenExpireAt;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public Long getTokenExpireAt() { return tokenExpireAt; }
        public void setTokenExpireAt(Long tokenExpireAt) { this.tokenExpireAt = tokenExpireAt; }
    }
}
