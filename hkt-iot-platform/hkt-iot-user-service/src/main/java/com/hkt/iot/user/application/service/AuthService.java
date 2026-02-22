package com.hkt.iot.user.application.service;

import com.hkt.iot.common.core.exception.BizException;
import com.hkt.iot.common.core.result.ResultCode;
import com.hkt.iot.common.security.jwt.JwtTokenProvider;
import com.hkt.iot.common.security.util.PasswordEncoderUtil;
import com.hkt.iot.user.domain.model.*;
import com.hkt.iot.user.domain.repository.*;
import com.hkt.iot.user.application.dto.*;
import com.hkt.iot.user.application.event.UserLoginEvent;
import com.hkt.iot.user.application.event.UserLogoutEvent;
import com.hkt.iot.user.domain.event.TenantCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 认证应用服务
 * 处理登录、登出、令牌刷新等认证相关功能
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final SsoSessionRepository ssoSessionRepository;
    private final MfaConfigRepository mfaConfigRepository;
    private final MfaChallengeRepository mfaChallengeRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 用户登录
     */
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        // 1. 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        // 2. 检查用户状态
        validateUserStatus(user);

        // 3. 检查租户状态
        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new BizException(ResultCode.TENANT_NOT_EXIST));
        if (tenant.getTenantStatus() == Tenant.TenantStatus.SUSPENDED) {
            throw new BizException(ResultCode.TENANT_SUSPENDED);
        }
        if (tenant.getTenantStatus() == Tenant.TenantStatus.TERMINATED) {
            throw new BizException(ResultCode.TENANT_TERMINATED);
        }

        // 4. 验证密码
        String encodedPassword = PasswordEncoderUtil.encode(request.getPassword(), user.getSalt());
        if (!user.verifyPassword(request.getPassword(), encodedPassword)) {
            user.recordFailedLogin();
            userRepository.save(user);
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }

        // 5. 检查是否启用MFA
        List<MfaConfig> mfaConfigs = mfaConfigRepository.findByUserId(user.getId());
        MfaConfig primaryMfa = mfaConfigs.stream()
                .filter(MfaConfig::isPrimary)
                .filter(MfaConfig::isConfigured)
                .findFirst()
                .orElse(null);

        if (primaryMfa != null && !request.isSkipMfa()) {
            // 创建MFA挑战
            return createMfaChallenge(user, tenant, primaryMfa);
        }

        // 6. 创建会话并生成令牌
        return createSessionAndToken(user, tenant, request.getClientId(),
                ipAddress, userAgent, request.getDeviceType(), request.getDeviceId());
    }

    /**
     * MFA验证
     */
    @Transactional
    public LoginResponse verifyMfa(MfaVerificationRequest request, String ipAddress) {
        // 1. 查找挑战
        MfaChallenge challenge = mfaChallengeRepository.findById(request.getChallengeId())
                .orElseThrow(() -> new BizException(ResultCode.MFA_CHALLENGE_INVALID));

        // 2. 检查挑战是否过期
        if (challenge.isExpired()) {
            throw new BizException(ResultCode.MFA_CHALLENGE_EXPIRED);
        }

        // 3. 验证MFA码
        if (!challenge.verifyCode(request.getCode())) {
            challenge.recordFailedAttempt();
            mfaChallengeRepository.save(challenge);
            throw new BizException(ResultCode.MFA_CODE_INVALID);
        }

        // 4. 获取用户和租户
        User user = userRepository.findById(challenge.getUserId())
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));
        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new BizException(ResultCode.TENANT_NOT_EXIST));

        // 5. 标记挑战已完成
        challenge.markAsVerified();
        mfaChallengeRepository.save(challenge);

        // 6. 创建会话并生成令牌
        return createSessionAndToken(user, tenant, challenge.getClientId(),
                ipAddress, challenge.getUserAgent(), challenge.getDeviceType(), challenge.getDeviceId());
    }

    /**
     * 创建MFA挑战
     */
    private LoginResponse createMfaChallenge(User user, Tenant tenant, MfaConfig mfaConfig) {
        MfaChallenge challenge = MfaChallenge.create(
                user.getId(),
                tenant.getId(),
                mfaConfig.getMfaType(),
                mfaConfig.getSecretKey(),
                "web-client",
                "browser",
                null,
                null,
                LocalDateTime.now().plusMinutes(5)
        );
        mfaChallengeRepository.save(challenge);

        return LoginResponse.builder()
                .requireMfa(true)
                .challengeId(challenge.getId().toString())
                .mfaType(mfaConfig.getMfaType().name())
                .message("请输入多因素验证码")
                .build();
    }

    /**
     * 创建会话并生成令牌
     */
    private LoginResponse createSessionAndToken(User user, Tenant tenant, String clientId,
            String ipAddress, String userAgent, String deviceType, String deviceId) {
        // 1. 记录登录
        user.recordLogin(ipAddress);
        userRepository.save(user);

        // 2. 创建SSO会话
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        String sessionToken = jwtTokenProvider.generateSessionToken(user.getId(), tenant.getId(), sessionId);

        SsoSession session = SsoSession.create(
                sessionId,
                sessionToken,
                user.getId(),
                tenant.getId(),
                clientId,
                deviceType,
                deviceId,
                ipAddress,
                userAgent,
                expiresAt
        );
        ssoSessionRepository.save(session);

        // 3. 生成JWT令牌
        List<String> roles = getUserRoles(user.getId());
        List<String> permissions = getUserPermissions(user.getId());

        String accessToken = jwtTokenProvider.generateToken(
                user.getId(),
                tenant.getId(),
                tenant.getTenantCode(),
                user.getUsername(),
                roles,
                permissions
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), sessionId);

        // 4. 发布登录事件
        eventPublisher.publishEvent(new UserLoginEvent(
                user.getId(),
                tenant.getId(),
                sessionId,
                ipAddress,
                LocalDateTime.now()
        ));

        // 5. 构建响应
        return LoginResponse.builder()
                .requireMfa(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(user.getId())
                .tenantId(tenant.getId())
                .tenantCode(tenant.getTenantCode())
                .tenantName(tenant.getTenantName())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roles(roles)
                .permissions(permissions)
                .sessionId(sessionId)
                .build();
    }

    /**
     * 刷新令牌
     */
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        // 1. 验证刷新令牌
        if (!jwtTokenProvider.validateRefreshToken(request.getRefreshToken())) {
            throw new BizException(ResultCode.TOKEN_INVALID);
        }

        // 2. 获取用户ID和会话ID
        Long userId = jwtTokenProvider.getUserIdFromRefreshToken(request.getRefreshToken());
        String sessionId = jwtTokenProvider.getSessionIdFromRefreshToken(request.getRefreshToken());

        // 3. 查找会话
        SsoSession session = ssoSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new BizException(ResultCode.SESSION_NOT_EXIST));

        if (!session.isValid()) {
            throw new BizException(ResultCode.SESSION_EXPIRED);
        }

        // 4. 更新会话活跃时间
        session.updateLastActiveTime();
        session.extend(LocalDateTime.now().plusHours(24));
        ssoSessionRepository.save(session);

        // 5. 获取用户和租户信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));
        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new BizException(ResultCode.TENANT_NOT_EXIST));

        // 6. 生成新令牌
        List<String> roles = getUserRoles(user.getId());
        List<String> permissions = getUserPermissions(user.getId());

        String accessToken = jwtTokenProvider.generateToken(
                user.getId(),
                tenant.getId(),
                tenant.getTenantCode(),
                user.getUsername(),
                roles,
                permissions
        );

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), sessionId);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .build();
    }

    /**
     * 登出
     */
    @Transactional
    public void logout(Long userId, String sessionId, String ipAddress) {
        // 1. 查找会话
        Optional<SsoSession> sessionOpt = ssoSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            SsoSession session = sessionOpt.get();
            session.logout();
            ssoSessionRepository.save(session);

            // 2. 发布登出事件
            eventPublisher.publishEvent(new UserLogoutEvent(
                    userId,
                    session.getTenantId(),
                    sessionId,
                    ipAddress,
                    LocalDateTime.now()
            ));
        }
    }

    /**
     * 验证用户状态
     */
    private void validateUserStatus(User user) {
        if (user.getUserStatus() == User.UserStatus.INACTIVE) {
            throw new BizException(ResultCode.USER_INACTIVE);
        }
        if (user.getUserStatus() == User.UserStatus.LOCKED) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new BizException(ResultCode.USER_LOCKED);
            } else {
                // 自动解锁
                user.unlock();
                userRepository.save(user);
            }
        }
    }

    /**
     * 获取用户角色
     */
    private List<String> getUserRoles(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        return userRoles.stream()
                .map(ur -> roleRepository.findById(ur.getRoleId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Role::getRoleCode)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户权限
     */
    private List<String> getUserPermissions(Long userId) {
        // TODO: 实现权限查询逻辑
        return Collections.emptyList();
    }
}
