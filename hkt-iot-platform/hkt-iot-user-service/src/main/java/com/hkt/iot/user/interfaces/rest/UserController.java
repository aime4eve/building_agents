package com.hkt.iot.user.interfaces.rest;

import com.hkt.iot.user.application.dto.*;
import com.hkt.iot.user.application.dto.UserDTO.*;
import com.hkt.iot.user.application.dto.CommonDTO;
import com.hkt.iot.user.application.service.UserApplicationService;
import com.hkt.iot.common.security.util.SecurityUtil;
import com.hkt.iot.common.core.page.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 *
 * @author HKT IoT Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息管理接口")
public class UserController {

    private final UserApplicationService userApplicationService;

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    @Operation(summary = "创建用户", description = "创建新用户")
    public CommonResponse<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        UserResponse response = userApplicationService.createUser(request, operatorId);
        return CommonResponse.success(response);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:update')")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public CommonResponse<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        UserResponse response = userApplicationService.updateUser(userId, request, operatorId);
        return CommonResponse.success(response);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:delete')")
    @Operation(summary = "删除用户", description = "软删除用户")
    public CommonResponse<Void> deleteUser(@PathVariable Long userId) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        userApplicationService.deleteUser(userId, operatorId);
        return CommonResponse.success();
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:view')")
    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
    public CommonResponse<UserResponse> getUser(@PathVariable Long userId) {
        UserResponse response = userApplicationService.getUser(userId);
        return CommonResponse.success(response);
    }

    /**
     * 根据用户名获取用户
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名获取用户", description = "根据用户名获取用户信息")
    public CommonResponse<UserResponse> getUserByUsername(@PathVariable String username) {
        UserResponse response = userApplicationService.getUserByUsername(username);
        return CommonResponse.success(response);
    }

    /**
     * 分页查询用户
     */
    @PostMapping("/search")
    @PreAuthorize("hasAuthority('user:view')")
    @Operation(summary = "分页查询用户", description = "根据条件分页查询用户列表")
    public CommonResponse<PageResponse<UserResponse>> searchUsers(
            @RequestBody UserQueryRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var page = userApplicationService.searchUsers(request, pageable);
        return CommonResponse.success(PageResponse.of(page));
    }

    /**
     * 获取租户下的用户列表
     */
    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasAuthority('user:view')")
    @Operation(summary = "获取租户用户列表", description = "获取指定租户下的用户列表")
    public CommonResponse<PageResponse<UserResponse>> getTenantUsers(
            @PathVariable Long tenantId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var page = userApplicationService.getTenantUsers(tenantId, pageable);
        return CommonResponse.success(PageResponse.of(page));
    }

    /**
     * 重置密码
     */
    @PostMapping("/{userId}/password/reset")
    @PreAuthorize("hasAuthority('user:manage')")
    @Operation(summary = "重置用户密码", description = "管理员重置用户密码")
    public CommonResponse<Void> resetPassword(
            @PathVariable Long userId,
            @RequestParam String newPassword) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        userApplicationService.resetPassword(userId, newPassword, operatorId);
        return CommonResponse.success();
    }

    /**
     * 修改密码
     */
    @PostMapping("/password/change")
    @Operation(summary = "修改密码", description = "用户修改自己的密码")
    public CommonResponse<Void> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        Long userId = SecurityUtil.getCurrentUserId();
        userApplicationService.changePassword(userId, oldPassword, newPassword);
        return CommonResponse.success();
    }

    /**
     * 激活用户
     */
    @PostMapping("/{userId}/activate")
    @PreAuthorize("hasAuthority('user:manage')")
    @Operation(summary = "激活用户", description = "激活已停用的用户")
    public CommonResponse<Void> activateUser(@PathVariable Long userId) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        userApplicationService.activateUser(userId, operatorId);
        return CommonResponse.success();
    }

    /**
     * 停用用户
     */
    @PostMapping("/{userId}/deactivate")
    @PreAuthorize("hasAuthority('user:manage')")
    @Operation(summary = "停用用户", description = "停用指定用户")
    public CommonResponse<Void> deactivateUser(@PathVariable Long userId) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        userApplicationService.deactivateUser(userId, operatorId);
        return CommonResponse.success();
    }

    /**
     * 解锁用户
     */
    @PostMapping("/{userId}/unlock")
    @PreAuthorize("hasAuthority('user:manage')")
    @Operation(summary = "解锁用户", description = "解锁已锁定的用户")
    public CommonResponse<Void> unlockUser(@PathVariable Long userId) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        userApplicationService.unlockUser(userId, operatorId);
        return CommonResponse.success();
    }

    /**
     * 分配角色
     */
    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('user:assign-role')")
    @Operation(summary = "分配角色", description = "为用户分配角色")
    public CommonResponse<Void> assignRoles(
            @PathVariable Long userId,
            @RequestBody List<Long> roleIds) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        userApplicationService.assignRoles(userId, roleIds, operatorId);
        return CommonResponse.success();
    }

    /**
     * 获取用户角色
     */
    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasAuthority('user:view')")
    @Operation(summary = "获取用户角色", description = "获取用户的所有角色")
    public CommonResponse<List<RoleResponse>> getUserRoles(@PathVariable Long userId) {
        List<RoleResponse> response = userApplicationService.getUserRoles(userId);
        return CommonResponse.success(response);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public CommonResponse<UserResponse> getCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        UserResponse response = userApplicationService.getUser(userId);
        return CommonResponse.success(response);
    }
}
