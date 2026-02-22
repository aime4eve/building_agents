package com.hkt.iot.device.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

/**
 * 设备注册响应DTO
 *
 * @author HKT IoT Team
 */
@Data
public class DeviceRegistryDTO {

    /**
     * 设备ID
     */
    @JsonProperty("deviceId")
    private String deviceId;

    /**
     * 设备序列号
     */
    @JsonProperty("deviceSn")
    private String deviceSn;

    /**
     * 设备证书
     */
    @JsonProperty("deviceCertificate")
    private DeviceCertificate deviceCertificate;

    /**
     * MQTT配置
     */
    @JsonProperty("mqttConfig")
    private MqttConfig mqttConfig;

    /**
     * 心跳间隔（秒）
     */
    @JsonProperty("heartbeatInterval")
    private Integer heartbeatInterval = 60;

    /**
     * 创建时间（毫秒时间戳）
     */
    @JsonProperty("createdAt")
    private Instant createdAt;

    /**
     * 设备证书
     */
    @Data
    public static class DeviceCertificate {
        /**
         * 客户端证书
         */
        @JsonProperty("clientCert")
        private String clientCert;

        /**
         * 客户端私钥
         */
        @JsonProperty("clientKey")
        private String clientKey;

        /**
         * CA证书
         */
        @JsonProperty("caCert")
        private String caCert;
    }

    /**
     * MQTT配置
     */
    @Data
    public static class MqttConfig {
        /**
         * MQTT Broker地址
         */
        @JsonProperty("broker")
        private String broker;

        /**
         * MQTT端口
         */
        @JsonProperty("port")
        private Integer port;

        /**
         * 协议
         */
        @JsonProperty("protocol")
        private String protocol;

        /**
         * JWT Token
         */
        @JsonProperty("token")
        private String token;

        /**
         * Token过期时间（毫秒时间戳）
         */
        @JsonProperty("tokenExpireAt")
        private Long tokenExpireAt;
    }
}
