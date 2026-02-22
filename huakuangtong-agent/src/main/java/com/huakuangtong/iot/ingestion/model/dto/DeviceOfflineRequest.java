package com.huakuangtong.iot.ingestion.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 设备离线请求
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Data
public class DeviceOfflineRequest {

    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    @NotBlank(message = "租户ID不能为空")
    private String tenantId;

    private Long disconnectedAt;

    private String reason;

    private Long lastCommunicatedAt;
}
