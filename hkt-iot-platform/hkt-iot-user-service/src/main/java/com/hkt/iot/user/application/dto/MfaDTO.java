package com.hkt.iot.user.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MFA相关DTO
 *
 * @author HKT IoT Team
 */
public class MfaDTO {

    /**
     * MFA设置响应
     */
    @Data
    @Builder
    public static class MfaSetupResponse {
        /**
         * 密钥
         */
        private String secret;

        /**
         * 二维码URL
         */
        private String qrCodeUrl;

        /**
         * 备用恢复码
         */
        private List<String> backupCodes;

        /**
         * 提示消息
         */
        private String message;
    }

    /**
     * MFA配置响应
     */
    @Data
    @Builder
    public static class MfaConfigResponse {
        private Long id;
        private Long userId;
        private String mfaType;
        private Boolean isEnabled;
        private Boolean isPrimary;
        private String status;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
    }

    /**
     * MFA设备注册请求
     */
    @Data
    @Builder
    public static class MfaDeviceRegisterRequest {
        /**
         * 设备类型
         */
        private String deviceType;

        /**
         * 设备名称
         */
        private String deviceName;

        /**
         * 设备标识符
         */
        private String deviceIdentifier;

        /**
         * 设备信息(JSON)
         */
        private String deviceInfo;
    }

    /**
     * MFA设备响应
     */
    @Data
    @Builder
    public static class MfaDeviceResponse {
        private Long id;
        private Long userId;
        private String deviceType;
        private String deviceName;
        private String deviceIdentifier;
        private Boolean isTrusted;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastUsedAt;
        private String status;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
    }
}
