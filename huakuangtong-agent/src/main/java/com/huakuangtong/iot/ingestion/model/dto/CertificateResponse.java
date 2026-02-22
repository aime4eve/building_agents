package com.huakuangtong.iot.ingestion.model.dto;

import lombok.Data;

/**
 * 证书响应
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Data
public class CertificateResponse {

    private DeviceCertificateResponse deviceCertificate;
    private Long expiresAt;
}
