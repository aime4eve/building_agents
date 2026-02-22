package com.hkt.iot.user.interfaces.rest;

import com.hkt.iot.user.application.dto.*;
import com.hkt.iot.user.application.dto.TenantDTO.*;
import com.hkt.iot.user.application.dto.CommonDTO;
import com.hkt.iot.user.application.service.TenantApplicationService;
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
 * 租户控制器
 *
 * @author HKT IoT Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "租户管理", description = "租户信息管理接口")
public class TenantController {

    private final TenantApplicationService tenantApplicationService;

    /**
     * 创建租户
     */
    @PostMapping
    @PreAuthorize("hasAuthority('tenant:create')")
    @Operation(summary = "创建租户", description = "创建新的租户")
    public CommonResponse<TenantResponse> createTenant(@Valid @RequestBody TenantCreateRequest request) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        TenantResponse response = tenantApplicationService.createTenant(request, operatorId);
        return CommonResponse.success(response);
    }

    /**
     * 更新租户
     */
    @PutMapping("/{tenantId}")
    @PreAuthorize("hasAuthority('tenant:update')")
    @Operation(summary = "更新租户", description = "更新租户信息")
    public CommonResponse<TenantResponse> updateTenant(
            @PathVariable Long tenantId,
            @Valid @RequestBody TenantUpdateRequest request) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        TenantResponse response = tenantApplicationService.updateTenant(tenantId, request, operatorId);
        return CommonResponse.success(response);
    }

    /**
     * 删除租户
     */
    @DeleteMapping("/{tenantId}")
    @PreAuthorize("hasAuthority('tenant:delete')")
    @Operation(summary = "删除租户", description = "软删除租户")
    public CommonResponse<Void> deleteTenant(@PathVariable Long tenantId) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        tenantApplicationService.deleteTenant(tenantId, operatorId);
        return CommonResponse.success();
    }

    /**
     * 获取租户详情
     */
    @GetMapping("/{tenantId}")
    @PreAuthorize("hasAuthority('tenant:view')")
    @Operation(summary = "获取租户详情", description = "根据ID获取租户详细信息")
    public CommonResponse<TenantResponse> getTenant(@PathVariable Long tenantId) {
        TenantResponse response = tenantApplicationService.getTenant(tenantId);
        return CommonResponse.success(response);
    }

    /**
     * 根据编码获取租户
     */
    @GetMapping("/code/{tenantCode}")
    @Operation(summary = "根据编码获取租户", description = "根据租户编码获取租户信息")
    public CommonResponse<TenantResponse> getTenantByCode(@PathVariable String tenantCode) {
        TenantResponse response = tenantApplicationService.getTenantByCode(tenantCode);
        return CommonResponse.success(response);
    }

    /**
     * 分页查询租户
     */
    @PostMapping("/search")
    @PreAuthorize("hasAuthority('tenant:view')")
    @Operation(summary = "分页查询租户", description = "根据条件分页查询租户列表")
    public CommonResponse<PageResponse<TenantResponse>> searchTenants(
            @RequestBody TenantQueryRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var page = tenantApplicationService.searchTenants(request, pageable);
        return CommonResponse.success(PageResponse.of(page));
    }

    /**
     * 获取子租户列表
     */
    @GetMapping("/{parentTenantId}/children")
    @PreAuthorize("hasAuthority('tenant:view')")
    @Operation(summary = "获取子租户列表", description = "获取指定租户的所有子租户")
    public CommonResponse<List<TenantResponse>> getChildrenTenants(@PathVariable Long parentTenantId) {
        List<TenantResponse> response = tenantApplicationService.getChildrenTenants(parentTenantId);
        return CommonResponse.success(response);
    }

    /**
     * 激活租户
     */
    @PostMapping("/{tenantId}/activate")
    @PreAuthorize("hasAuthority('tenant:manage')")
    @Operation(summary = "激活租户", description = "激活已暂停的租户")
    public CommonResponse<Void> activateTenant(@PathVariable Long tenantId) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        tenantApplicationService.activateTenant(tenantId, operatorId);
        return CommonResponse.success();
    }

    /**
     * 暂停租户
     */
    @PostMapping("/{tenantId}/suspend")
    @PreAuthorize("hasAuthority('tenant:manage')")
    @Operation(summary = "暂停租户", description = "暂停指定租户")
    public CommonResponse<Void> suspendTenant(@PathVariable Long tenantId) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        tenantApplicationService.suspendTenant(tenantId, operatorId);
        return CommonResponse.success();
    }

    /**
     * 更新租户配额
     */
    @PutMapping("/{tenantId}/quota")
    @PreAuthorize("hasAuthority('tenant:manage')")
    @Operation(summary = "更新租户配额", description = "更新租户的配额限制")
    public CommonResponse<Void> updateTenantQuota(
            @PathVariable Long tenantId,
            @Valid @RequestBody TenantQuotaRequest request) {
        Long operatorId = SecurityUtil.getCurrentUserId();
        tenantApplicationService.updateTenantQuota(tenantId, request, operatorId);
        return CommonResponse.success();
    }
}
