package com.hkt.iot.user.application.service;

import com.hkt.iot.common.core.exception.BizException;
import com.hkt.iot.common.core.result.ResultCode;
import com.hkt.iot.common.security.util.PasswordEncoderUtil;
import com.hkt.iot.user.domain.model.Tenant;
import com.hkt.iot.user.domain.model.User;
import com.hkt.iot.user.domain.repository.TenantRepository;
import com.hkt.iot.user.domain.repository.UserRepository;
import com.hkt.iot.user.domain.repository.UserRoleRepository;
import com.hkt.iot.user.domain.repository.RoleRepository;
import com.hkt.iot.user.domain.model.UserRole;
import com.hkt.iot.user.application.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户应用服务
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 创建用户
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request, Long operatorId) {
        // 1. 检查租户是否存在
        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new BizException(ResultCode.TENANT_NOT_EXIST));

        // 2. 检查租户状态
        if (tenant.getTenantStatus() != Tenant.TenantStatus.ACTIVE) {
            throw new BizException(ResultCode.TENANT_INACTIVE);
        }

        // 3. 检查用户名是否存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BizException(ResultCode.USERNAME_EXIST);
        }

        // 4. 检查邮箱是否存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BizException(ResultCode.EMAIL_EXIST);
        }

        // 5. 检查用户配额
        long currentUserCount = userRepository.countByTenantId(request.getTenantId());
        if (!tenant.checkUserQuota(currentUserCount)) {
            throw new BizException(ResultCode.USER_QUOTA_EXCEEDED);
        }

        // 6. 生成密码和盐值
        String salt = UUID.randomUUID().toString().replace("-", "");
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 7. 创建用户
        User user = User.create(
                request.getTenantId(),
                tenant.getTenantCode(),
                request.getUsername(),
                request.getRealName(),
                request.getEmail(),
                request.getPhone(),
                encodedPassword,
                salt,
                operatorId
        );

        // 8. 设置附加信息
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getPosition() != null) {
            user.setPosition(request.getPosition());
        }
        if (request.getEmployeeId() != null) {
            user.setEmployeeId(request.getEmployeeId());
        }

        // 9. 保存用户
        user = userRepository.save(user);

        // 10. 分配默认角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long roleId : request.getRoleIds()) {
                UserRole userRole = UserRole.create(user.getId(), roleId);
                userRoleRepository.save(userRole);
            }
        }

        // 11. 发布领域事件
        user.getDomainEvents().forEach(eventPublisher::publishEvent);
        user.clearDomainEvents();

        log.info("创建用户成功: userId={}, username={}, tenantId={}",
                user.getId(), user.getUsername(), user.getTenantId());

        return toUserResponse(user, tenant);
    }

    /**
     * 更新用户
     */
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request, Long operatorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        // 更新基本信息
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getEmail() != null) {
            // 检查邮箱是否被其他用户使用
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
                throw new BizException(ResultCode.EMAIL_EXIST);
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getPosition() != null) {
            user.setPosition(request.getPosition());
        }
        if (request.getEmployeeId() != null) {
            user.setEmployeeId(request.getEmployeeId());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        user.setUpdatedBy(operatorId);
        user = userRepository.save(user);

        log.info("更新用户成功: userId={}", userId);

        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new BizException(ResultCode.TENANT_NOT_EXIST));

        return toUserResponse(user, tenant);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long userId, Long operatorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        user.softDelete(operatorId);
        userRepository.save(user);

        // 删除用户角色关联
        userRoleRepository.deleteByUserId(userId);

        log.info("删除用户成功: userId={}", userId);
    }

    /**
     * 获取用户详情
     */
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new BizException(ResultCode.TENANT_NOT_EXIST));

        return toUserResponse(user, tenant);
    }

    /**
     * 根据用户名获取用户
     */
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        Tenant tenant = tenantRepository.findById(user.getTenantId())
                .orElseThrow(() -> new BizException(ResultCode.TENANT_NOT_EXIST));

        return toUserResponse(user, tenant);
    }

    /**
     * 分页查询用户
     */
    public Page<UserResponse> searchUsers(UserQueryRequest request, Pageable pageable) {
        return userRepository.search(request, pageable)
                .map(user -> {
                    Tenant tenant = tenantRepository.findById(user.getTenantId()).orElse(null);
                    return toUserResponse(user, tenant);
                });
    }

    /**
     * 获取租户下的用户列表
     */
    public Page<UserResponse> getTenantUsers(Long tenantId, Pageable pageable) {
        return userRepository.findByTenantId(tenantId, pageable)
                .map(user -> {
                    Tenant tenant = tenantRepository.findById(user.getTenantId()).orElse(null);
                    return toUserResponse(user, tenant);
                });
    }

    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword, Long operatorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        String salt = UUID.randomUUID().toString().replace("-", "");
        String encodedPassword = passwordEncoder.encode(newPassword);

        user.updatePassword(encodedPassword, salt);
        user.setUpdatedBy(operatorId);
        userRepository.save(user);

        log.info("重置用户密码成功: userId={}", userId);
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException(ResultCode.OLD_PASSWORD_ERROR);
        }

        String salt = UUID.randomUUID().toString().replace("-", "");
        String encodedPassword = passwordEncoder.encode(newPassword);

        user.updatePassword(encodedPassword, salt);
        userRepository.save(user);

        log.info("用户修改密码成功: userId={}", userId);
    }

    /**
     * 激活用户
     */
    @Transactional
    public void activateUser(Long userId, Long operatorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        user.activate();
        user.setUpdatedBy(operatorId);
        userRepository.save(user);

        log.info("激活用户成功: userId={}", userId);
    }

    /**
     * 停用用户
     */
    @Transactional
    public void deactivateUser(Long userId, Long operatorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        user.deactivate();
        user.setUpdatedBy(operatorId);
        userRepository.save(user);

        log.info("停用用户成功: userId={}", userId);
    }

    /**
     * 解锁用户
     */
    @Transactional
    public void unlockUser(Long userId, Long operatorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        user.unlock();
        user.setUpdatedBy(operatorId);
        userRepository.save(user);

        log.info("解锁用户成功: userId={}", userId);
    }

    /**
     * 分配角色
     */
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds, Long operatorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ResultCode.USER_NOT_EXIST));

        // 删除现有角色
        userRoleRepository.deleteByUserId(userId);

        // 分配新角色
        for (Long roleId : roleIds) {
            if (!roleRepository.existsById(roleId)) {
                throw new BizException(ResultCode.ROLE_NOT_EXIST);
            }
            UserRole userRole = UserRole.create(userId, roleId);
            userRoleRepository.save(userRole);
        }

        user.setUpdatedBy(operatorId);
        userRepository.save(user);

        log.info("分配用户角色成功: userId={}, roleIds={}", userId, roleIds);
    }

    /**
     * 获取用户角色
     */
    public List<RoleResponse> getUserRoles(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);

        return userRoles.stream()
                .map(ur -> roleRepository.findById(ur.getRoleId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::toRoleResponse)
                .collect(Collectors.toList());
    }

    /**
     * 转换为响应对象
     */
    private UserResponse toUserResponse(User user, Tenant tenant) {
        return UserResponse.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .tenantCode(user.getTenantCode())
                .tenantName(tenant != null ? tenant.getTenantName() : null)
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .userStatus(user.getUserStatus().name())
                .accountType(user.getAccountType() != null ? user.getAccountType().name() : null)
                .isMfaEnabled(user.getIsMfaEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .avatar(user.getAvatar())
                .department(user.getDepartment())
                .position(user.getPosition())
                .employeeId(user.getEmployeeId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private RoleResponse toRoleResponse(com.hkt.iot.user.domain.model.Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .tenantId(role.getTenantId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .roleType(role.getRoleType().name())
                .description(role.getDescription())
                .isDefault(role.getIsDefault())
                .status(role.getStatus().name())
                .build();
    }
}
