package com.hkt.iot.user.domain.repository;

import com.hkt.iot.domain.repository.OptimisticLockRepository;
import com.hkt.iot.user.domain.model.Tenant;
import com.hkt.iot.user.domain.model.Tenant.TenantStatus;
import com.hkt.iot.user.domain.model.Tenant.TenantType;

import java.util.List;
import java.util.Optional;

/**
 * 租户仓储接口
 * 基于DDD设计，提供租户聚合根的持久化操作
 *
 * @author HKT IoT Team
 */
public interface TenantRepository extends OptimisticLockRepository<Tenant, Long> {

    /**
     * 根据租户编码查找
     *
     * @param tenantCode 租户编码
     * @return 租户
     */
    Optional<Tenant> findByTenantCode(String tenantCode);

    /**
     * 根据租户类型查找
     *
     * @param tenantType 租户类型
     * @return 租户列表
     */
    List<Tenant> findByTenantType(TenantType tenantType);

    /**
     * 根据父租户ID查找子租户
     *
     * @param parentTenantId 父租户ID
     * @return 子租户列表
     */
    List<Tenant> findByParentTenantId(Long parentTenantId);

    /**
     * 根据租户状态查找
     *
     * @param tenantStatus 租户状态
     * @return 租户列表
     */
    List<Tenant> findByTenantStatus(TenantStatus tenantStatus);

    /**
     * 查找即将到期的租户
     *
     * @param beforeDate 到期日期
     * @return 租户列表
     */
    List<Tenant> findByExpireDateBefore(java.time.LocalDateTime beforeDate);

    /**
     * 检查租户编码是否存在
     *
     * @param tenantCode 租户编码
     * @return 是否存在
     */
    boolean existsByTenantCode(String tenantCode);

    /**
     * 统计租户数量
     *
     * @return 租户数量
     */
    long count();
}
