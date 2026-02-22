package com.hkt.iot.smartapps.moldprevention.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 防霉管控区域 Spring Data JPA Repository
 */
@Repository
public interface MoldPreventionZoneJpaRepository extends JpaRepository<MoldPreventionZoneEntity, Long> {

    /**
     * 根据租户 ID 查询
     */
    @Query("SELECT z FROM MoldPreventionZoneEntity z WHERE z.tenantId = :tenantId AND z.deleted = 0 ORDER BY z.createdAt DESC")
    List<MoldPreventionZoneEntity> findByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据空间 ID 查询
     */
    @Query("SELECT z FROM MoldPreventionZoneEntity z WHERE z.spaceId = :spaceId AND z.deleted = 0")
    List<MoldPreventionZoneEntity> findBySpaceId(@Param("spaceId") Long spaceId);

    /**
     * 查询活跃区域
     */
    @Query("SELECT z FROM MoldPreventionZoneEntity z WHERE z.tenantId = :tenantId AND z.zoneStatus = 'ACTIVE' AND z.deleted = 0")
    List<MoldPreventionZoneEntity> findActiveZones(@Param("tenantId") Long tenantId);

    /**
     * 根据 ID 查询 (包含软删除检查)
     */
    @Query("SELECT z FROM MoldPreventionZoneEntity z WHERE z.id = :id AND z.deleted = 0")
    Optional<MoldPreventionZoneEntity> findByIdWithDeletedCheck(@Param("id") Long id);

    /**
     * 检查区域编码是否存在
     */
    @Query("SELECT COUNT(z) > 0 FROM MoldPreventionZoneEntity z WHERE z.zoneCode = :zoneCode AND z.tenantId = :tenantId AND z.deleted = 0")
    boolean existsByZoneCodeAndTenantId(@Param("zoneCode") String zoneCode, @Param("tenantId") Long tenantId);
}
