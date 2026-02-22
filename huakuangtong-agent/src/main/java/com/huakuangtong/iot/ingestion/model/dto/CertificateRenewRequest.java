package com.huakuangtong.iot.ingestion.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 证书续期请求
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Data
public class CertificateRenewRequest {

    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    private String oldCertSn;
}
