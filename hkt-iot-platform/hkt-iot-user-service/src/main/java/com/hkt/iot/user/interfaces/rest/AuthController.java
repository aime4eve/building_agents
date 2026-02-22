package com.hkt.iot.user.interfaces.rest;

import com.hkt.iot.user.application.dto.*;
import com.hkt.iot.user.application.dto.AuthDTO.*;
import com.hkt.iot.user.application.dto.CommonDTO;
import com.hkt.iot.user.application.service.AuthService;
import com.hkt.iot.common.security.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author HKT IoT Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、登出、令牌刷新等认证相关接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，支持MFA多因素认证")
    public CommonResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.login(request, ipAddress, userAgent);
        return CommonResponse.success(response);
    }

    /**
     * MFA验证
     */
    @PostMapping("/mfa/verify")
    @Operation(summary = "MFA验证", description = "多因素认证验证")
    public CommonResponse<LoginResponse> verifyMfa(
            @Valid @RequestBody MfaVerificationRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);

        LoginResponse response = authService.verifyMfa(request, ipAddress);
        return CommonResponse.success(response);
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public CommonResponse<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refreshToken(request);
        return CommonResponse.success(response);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出登录并销毁会话")
    public CommonResponse<Void> logout(
            @RequestBody LogoutRequest request,
            HttpServletRequest httpRequest) {
        Long userId = SecurityUtil.getCurrentUserId();
        String ipAddress = getClientIp(httpRequest);

        authService.logout(userId, request.getSessionId(), ipAddress);
        return CommonResponse.success();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
