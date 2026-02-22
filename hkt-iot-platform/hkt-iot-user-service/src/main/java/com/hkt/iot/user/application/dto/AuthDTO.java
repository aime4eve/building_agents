package com.hkt.iot.user.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证相关DTO
 *
 * @author HKT IoT Team
 */
public class AuthDTO {

    /**
     * 登录请求
     */
    @Data
    @Builder
    public static class LoginRequest {
        /**
         * 用户名
         */
        private String username;

        /**
         * 密码
         */
        private String password;

        /**
         * 客户端ID
         */
        private String clientId;

        /**
         * 设备类型
         */
        private String deviceType;

        /**
         * 设备ID
         */
        private String deviceId;

        /**
         * 是否跳过MFA验证
         */
        private Boolean skipMfa;
    }

    /**
     * 登录响应
     */
    @Data
    @Builder
    public static class LoginResponse {
        /**
         * 是否需要MFA验证
         */
        private Boolean requireMfa;

        /**
         * 挑战ID
         */
        private String challengeId;

        /**
         * MFA类型
         */
        private String mfaType;

        /**
         * 提示消息
         */
        private String message;

        /**
         * 访问令牌
         */
        private String accessToken;

        /**
         * 刷新令牌
         */
        private String refreshToken;

        /**
         * 令牌类型
         */
        private String tokenType;

        /**
         * 过期时间(秒)
         */
        private Long expiresIn;

        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 租户ID
         */
        private Long tenantId;

        /**
         * 租户编码
         */
        private String tenantCode;

        /**
         * 租户名称
         */
        private String tenantName;

        /**
         * 用户名
         */
        private String username;

        /**
         * 真实姓名
         */
        private String realName;

        /**
         * 角色列表
         */
        private List<String> roles;

        /**
         * 权限列表
         */
        private List<String> permissions;

        /**
         * 会话ID
         */
        private String sessionId;
    }

    /**
     * MFA验证请求
     */
    @Data
    @Builder
    public static class MfaVerificationRequest {
        /**
         * 挑战ID
         */
        private String challengeId;

        /**
         * 验证码
         */
        private String code;
    }

    /**
     * 刷新令牌请求
     */
    @Data
    @Builder
    public static class RefreshTokenRequest {
        /**
         * 刷新令牌
         */
        private String refreshToken;
    }

    /**
     * 令牌响应
     */
    @Data
    @Builder
    public static class TokenResponse {
        /**
         * 访问令牌
         */
        private String accessToken;

        /**
         * 刷新令牌
         */
        private String refreshToken;

        /**
         * 令牌类型
         */
        private String tokenType;

        /**
         * 过期时间(秒)
         */
        private Long expiresIn;
    }

    /**
     * 登出请求
     */
    @Data
    @Builder
    public static class LogoutRequest {
        /**
         * 会话ID
         */
        private String sessionId;
    }
}
