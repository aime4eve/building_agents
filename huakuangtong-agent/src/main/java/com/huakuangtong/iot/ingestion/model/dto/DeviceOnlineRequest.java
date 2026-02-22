package com.huakuangtong.iot.ingestion.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 设备上线请求
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Data
public class DeviceOnlineRequest {

    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    @NotBlank(message = "租户ID不能为空")
    private String tenantId;

    private String clientIp;

    private Long connectedAt;

    private ConnectionInfo connectionInfo;

    @Data
    public static class ConnectionInfo {
        private String ipAddress;
        private String protocol;
        private String protocolVersion;
        private String clientVersion;
    }
}
