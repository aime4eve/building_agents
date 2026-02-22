package com.huakuangtong.iot.ingestion.model.dto;

import lombok.Data;

/**
 * 设备注册响应
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Data
public class DeviceRegisterResponse {

    private String deviceId;
    private String deviceSn;
    private String deviceType;
    private DeviceCertificateResponse deviceCertificate;
    private MqttConfigResponse mqttConfig;
    private JwtTokenResponse jwtToken;
    private Long createdAt;
}
