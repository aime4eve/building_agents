package com.huakuangtong.iot.ingestion.model.dto;

import lombok.Data;

/**
 * 设备证书响应
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Data
public class DeviceCertificateResponse {

    private String clientCert;
    private String clientKey;
    private String caCert;
    private Long expiresAt;
}
