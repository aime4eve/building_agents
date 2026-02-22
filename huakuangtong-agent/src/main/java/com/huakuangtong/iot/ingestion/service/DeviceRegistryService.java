package com.huakuangtong.iot.ingestion.service;

import com.huakuangtong.iot.ingestion.model.dto.*;

/**
 * 设备注册服务
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
public interface DeviceRegistryService {

    /**
     * 注册设备
     */
    DeviceRegisterResponse registerDevice(DeviceRegisterRequest request);

    /**
     * 续期证书
     */
    CertificateResponse renewCertificate(CertificateRenewRequest request);

    /**
     * 刷新Token
     */
    TokenRefreshResponse refreshToken(TokenRefreshRequest request);

    /**
     * 处理设备上线
     */
    void handleDeviceOnline(DeviceOnlineRequest request);

    /**
     * 处理设备离线
     */
    void handleDeviceOffline(DeviceOfflineRequest request);
}
