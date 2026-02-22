package com.huakuangtong.iot.ingestion.controller;

import com.huakuangtong.iot.ingestion.model.dto.DeviceRegisterRequest;
import com.huakuangtong.iot.ingestion.model.dto.DeviceRegisterResponse;
import com.huakuangtong.iot.ingestion.model.dto.TokenRefreshRequest;
import com.huakuangtong.iot.ingestion.model.dto.TokenRefreshResponse;
import com.huakuangtong.iot.ingestion.service.DeviceRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 设备注册API Controller
 *
 * 负责设备注册、证书管理、Token刷新
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/device-ingestion")
@RequiredArgsConstructor
public class DeviceRegistryController {

    private final DeviceRegistryService deviceRegistryService;

    /**
     * 设备注册
     *
     * POST /api/v1/device-ingestion/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<DeviceRegisterResponse>> register(
            @Valid @RequestBody DeviceRegisterRequest request) {

        log.info("Device registration request, deviceSn: {}, deviceType: {}",
            request.getDeviceSn(), request.getDeviceType());

        try {
            DeviceRegisterResponse response = deviceRegistryService.registerDevice(request);

            log.info("Device registered successfully, deviceId: {}", response.getDeviceId());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid device registration request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(40001, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to register device", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(50001, "设备注册失败"));
        }
    }

    /**
     * 证书续期
     *
     * POST /api/v1/device-ingestion/renew-cert
     */
    @PostMapping("/renew-cert")
    public ResponseEntity<ApiResponse<CertificateResponse>> renewCertificate(
            @RequestBody CertificateRenewRequest request) {

        log.info("Certificate renewal request, deviceId: {}", request.getDeviceId());

        try {
            CertificateResponse response = deviceRegistryService.renewCertificate(request);

            log.info("Certificate renewed successfully, deviceId: {}", request.getDeviceId());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid certificate renewal request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(40004, e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to renew certificate", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(50001, "证书续期失败"));
        }
    }

    /**
     * Token刷新
     *
     * POST /api/v1/device-ingestion/refresh-token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {

        log.debug("Token refresh request");

        try {
            TokenRefreshResponse response = deviceRegistryService.refreshToken(request);

            log.debug("Token refreshed successfully");

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid token refresh request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(40102, "Token刷新失败"));
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(50001, "Token刷新失败"));
        }
    }

    /**
     * 设备上线通知（EMQX钩子调用）
     *
     * POST /api/v1/device-ingestion/status/online
     */
    @PostMapping("/status/online")
    public ResponseEntity<ApiResponse<Void>> deviceOnline(
            @RequestBody DeviceOnlineRequest request) {

        log.info("Device online, deviceId: {}, ip: {}", request.getDeviceId(), request.getClientIp());

        try {
            deviceRegistryService.handleDeviceOnline(request);
            return ResponseEntity.ok(ApiResponse.success(null));

        } catch (Exception e) {
            log.error("Failed to handle device online", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(50001, "设备上线处理失败"));
        }
    }

    /**
     * 设备离线通知（EMQX钩子调用）
     *
     * POST /api/v1/device-ingestion/status/offline
     */
    @PostMapping("/status/offline")
    public ResponseEntity<ApiResponse<Void>> deviceOffline(
            @RequestBody DeviceOfflineRequest request) {

        log.info("Device offline, deviceId: {}, reason: {}", request.getDeviceId(), request.getReason());

        try {
            deviceRegistryService.handleDeviceOffline(request);
            return ResponseEntity.ok(ApiResponse.success(null));

        } catch (Exception e) {
            log.error("Failed to handle device offline", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(50001, "设备离线处理失败"));
        }
    }
}
