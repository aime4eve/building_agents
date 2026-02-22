package com.hkt.iot.user.application.service;

import com.hkt.iot.common.core.exception.BizException;
import com.hkt.iot.common.core.result.ResultCode;
import com.hkt.iot.user.domain.model.Tenant;
import com.hkt.iot.user.domain.model.User;
import com.hkt.iot.user.domain.repository.TenantRepository;
import com.hkt.iot.user.domain.repository.UserRepository;
import com.hkt.iot.user.application.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 租户应用服务
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantApplicationService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 创建租户
     */
    @Transactional
    public TenantResponse createTenant(TenantCreateRequest request, Long operatorId) {
        // 1. 检查租户编码是否存在
        if (tenantRepository.existsByTenantCode(request.getTenantCode())) {
            throw new BizException(ResultCode.TENANT_CODE_EXIST);
        }

        // 2. 检查父租户是否存在
        if (request.getParentTenantId() != null) {
            Tenant parentTenant = tenantRepository.findById(request.getParentTenantId())
                    .orElseThrow(() -> new BizException(ResultCode.PARENT_TENANT_NOT_EXIST));

            if (parentTenant.getTenantStatus() != Tenant.TenantStatus.ACTIVE) {
                throw new BizException(ResultCode.PARENT_TENANT_INACTIVE);
            }
        }

        // 3. 创建租户
        Tenant tenant = Tenant.create(
                request.getTenantCode(),
                request.getTenantName(),
                request.getTenantType(),
                request.getContactPerson(),
                request.getContactPhone(),
                request.getContactEmail(),
                operatorId
        );

        // 4. 设置父租户
        if (request.getParentTenantId() != null) {
            Tenant parentTenant = tenantRepository.findById(request.getParentTenantId()).get();
            tenant.setParentTenant(
                    parentTenant.getId(),
                    parentTenant.getTenantPath(),
                    parentTenant.getTenantLevel()
            );
        }

        // 5. 设置行业和公司信息
        if (request.getIndustry() != null) {
            tenant.setIndustry(request.getIndustry());
        }
        if (request.getCompanySize() != null) {
            tenant.setCompanySize(request.getCompanySize());
        }
        if (request.getBusinessLicense() != null) {
            tenant.setBusinessLicense(request.getBusinessLicense());
        }
        if (request.getAddress() != null) {
            tenant.setAddress(request.getAddress());
        }

        // 6. 设置配额
        if (request.getMaxUsers() != null) {
            tenant.setMaxUsers(request.getMaxUsers());
        }
        if (request.getMaxDevices() != null) {
            tenant.setMaxDevices(request.getMaxDevices());
        }
        if (request.getMaxSpaces() != null) {
            tenant.setMaxSpaces(request.getMaxSpaces());
        }
        if (request.getStorageQuota() != null) {
            tenant.setStorageQuota(request.getStorageQuota());
        }

        // 7. 保存租户
        tenant = tenantRepository.save(tenant);

        // 8. 发布领域事件
        tenant.getDomainEvents().forEach(eventPublisher::publishEvent);
        tenant.clearDomainEvents();

        log.info("创建租户成功: tenantId={}, tenantCode={}", tenant.getId(), tenant.getTenantCode());

        return toTenantResponse(tenant);
    }

    /**
     * 更新租户
     */
    @Transactional
    public TenantResponse updateTenant(Long tenantId, TenantUpdateRequest request, Long operatorId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BizException(ResultCode.TENANT_NOT_EXIST));

        // 更新基本信息
        if (request.getTenantName() != null) {
            tenant.setTenantName(request.getTenantName());
        }
        if (request.getContactPerson() != null) {
            tenant.setContactPerson(request.getContactPerson());
        }
        if (request.getContactPhone() != null) {
            tenant.setContactPhone(request.getContactPhone());
        }
        if (request.getContactEmail() != null) {
            tenant.setContactEmail(request.getContactEmail());
        }
        if (request.getAddress() != null) {
            tenant.setAddress(request.getAddress());
        }
        if (request.getIndustry() != null) {
            tenant.setIndustry(request.getIndustry());
        }
        if (request.getCompanySize() != null) {
            tenant.setCompanySize(request.getCompanySize());
        }
        if (request.getBusinessLicense() != null) {
            tenant.setBusinessLicense(request.getBusinessLicense());
        }

        tenant.setUpdatedBy(operatorId);
        tenant = tenantRepository.save(tenant);

        log.info("更新租户成功: tenantId={}", tenantId);

        return toTenantResponse(tenant);
    }

    /**
     * 删除租户
     */
    @Transactional
    public void deleteTenant(Long tenantId, Long operatorId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BizException(ResultCode.TENANT_NOT_EXIST));

        // 检查是否有子租户
        if (tenantRepository.existsByParentTenantId(tenantId)) {
            throw new BizException(ResultCode.TENANT_HAS_CHILDREN);
        }

        // 检查是否有用户
        long userCount = userRepository.countByTenantId(tenantId);
        if (userCount > 0) {
            throw new BizException(ResultCode.TENANT_HAS_USERS);
        }

        tenant.softDelete(operatorId);
        tenantRepository.save(tenant);

        log.info("删除租户成功: tenantId={}", tenantId);
    }

    /**
     * 获取租户详情
     */
    public TenantResponse getTenant(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BizException(ResultCode.TENANT_NOT_EXIST));

        // 获取统计信息
        long userCount = userRepository.countByTenantId(tenantId);

        TenantResponse response = toTenantResponse(tenant);
        response.setCurrentUserCount((int) userCount);

        return response;
    }

    /**
     * 根据租户编码获取租户
     */
    public TenantResponse getTenantByCode(String tenantCode) {
        Tenant tenant = tenantRepository.findByTenantCode(tenantCode)
                .orElseThrow(() -> new BizException(ResultCode.TENANT_NOT_EXIST));

        return toTenantResponse(tenant);
    }

    /**
     * 分页查询租户
     */
    public Page<TenantResponse> searchTenants(TenantQueryRequest request, Pageable pageable) {
        return tenantRepository.search(request, pageable)
                .map(this::toTenantResponse);
    }

    /**
     * 获取子租户列表
     */
    public List<TenantResponse> getChildrenTenants(Long parentTenantId) {
        return tenantRepository.findByParentTenantId(parentTenantId).stream()
                .map(this::toTenantResponse)
                .collect(Collectors.toList());
    }

    /**
     * 激活租户
     */
    @Transactional
    public void activateTenant(Long tenantId, Long operatorId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BizException(ResultCode.TENANT_NOT_EXIST));

        tenant.activate();
        tenant.setUpdatedBy(operatorId);
        tenantRepository.save(tenant);

        log.info("激活租户成功: tenantId={}", tenantId);
    }

    /**
     * 暂停租户
     */
    @Transactional
    public void suspendTenant(Long tenantId, Long operatorId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BizException(ResultCode.TENANT_NOT_EXIST));

        tenant.suspend();
        tenant.setUpdatedBy(operatorId);
        tenantRepository.save(tenant);

        log.info("暂停租户成功: tenantId={}", tenantId);
    }

    /**
     * 更新租户配额
     */
    @Transactional
    public void updateTenantQuota(Long tenantId, TenantQuotaRequest request, Long operatorId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BizException(ResultCode.TENANT_NOT_EXIST));

        tenant.updateQuota(
                request.getMaxUsers(),
                request.getMaxDevices(),
                request.getMaxSpaces(),
                request.getStorageQuota()
        );
        tenant.setUpdatedBy(operatorId);
        tenantRepository.save(tenant);

        log.info("更新租户配额成功: tenantId={}", tenantId);
    }

    /**
     * 转换为响应对象
     */
    private TenantResponse toTenantResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .tenantCode(tenant.getTenantCode())
                .tenantName(tenant.getTenantName())
                .tenantType(tenant.getTenantType().name())
                .parentTenantId(tenant.getParentTenantId())
                .tenantPath(tenant.getTenantPath())
                .tenantLevel(tenant.getTenantLevel())
                .contactPerson(tenant.getContactPerson())
                .contactPhone(tenant.getContactPhone())
                .contactEmail(tenant.getContactEmail())
                .address(tenant.getAddress())
                .industry(tenant.getIndustry())
                .companySize(tenant.getCompanySize())
                .businessLicense(tenant.getBusinessLicense())
                .maxUsers(tenant.getMaxUsers())
                .maxDevices(tenant.getMaxDevices())
                .maxSpaces(tenant.getMaxSpaces())
                .storageQuota(tenant.getStorageQuota())
                .tenantStatus(tenant.getTenantStatus().name())
                .activateDate(tenant.getActivateDate())
                .expireDate(tenant.getExpireDate())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
