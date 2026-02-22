package com.hkt.iot.smartapps.moldprevention.domain.repository;

import com.hkt.iot.domain.shared.SpaceId;
import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.smartapps.moldprevention.domain.model.MoldPreventionZone;
import com.hkt.iot.smartapps.moldprevention.domain.model.ZoneId;

import java.util.List;
import java.util.Optional;

/**
 * 防霉管控区域仓储接口
 */
public interface MoldPreventionZoneRepository {

    /**
     * 保存防霉管控区域
     *
     * @param zone 防霉管控区域
     */
    void save(MoldPreventionZone zone);

    /**
     * 按ID查询
     *
     * @param id 区域ID
     * @return 防霉管控区域
     */
    Optional<MoldPreventionZone> findById(ZoneId id);

    /**
     * 按租户ID查询
     *
     * @param tenantId 租户ID
     * @return 防霉管控区域列表
     */
    List<MoldPreventionZone> findByTenantId(TenantId tenantId);

    /**
     * 按空间ID查询
     *
     * @param spaceId 空间ID
     * @return 防霉管控区域列表
     */
    List<MoldPreventionZone> findBySpaceId(SpaceId spaceId);

    /**
     * 查询活跃区域
     *
     * @param tenantId 租户ID
     * @return 活跃的防霉管控区域列表
     */
    List<MoldPreventionZone> findActiveZones(TenantId tenantId);

    /**
     * 删除防霉管控区域
     *
     * @param id 区域ID
     */
    void delete(ZoneId id);

    /**
     * 检查区域编码是否存在
     *
     * @param code 区域编码
     * @param tenantId 租户ID
     * @return 是否存在
     */
    boolean existsByCode(String code, TenantId tenantId);
}
