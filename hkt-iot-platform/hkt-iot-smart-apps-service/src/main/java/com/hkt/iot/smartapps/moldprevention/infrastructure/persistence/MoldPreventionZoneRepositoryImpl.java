package com.hkt.iot.smartapps.moldprevention.infrastructure.persistence;

import com.hkt.iot.domain.shared.SpaceId;
import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.smartapps.moldprevention.domain.model.*;
import com.hkt.iot.smartapps.moldprevention.domain.repository.MoldPreventionZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 防霉管控区域仓储实现
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class MoldPreventionZoneRepositoryImpl implements MoldPreventionZoneRepository {

    private final MoldPreventionZoneJpaRepository jpaRepository;

    @Override
    @Transactional
    public void save(MoldPreventionZone zone) {
        log.debug("保存防霉管控区域：zoneId={}, zoneCode={}", 
            zone.getId() != null ? zone.getId().getValue() : "new", 
            zone.getCode() != null ? zone.getCode().getValue() : "N/A");
        
        MoldPreventionZoneEntity entity = MoldPreventionZoneEntity.fromDomain(zone);
        jpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MoldPreventionZone> findById(ZoneId id) {
        log.debug("查询防霉管控区域：id={}", id.getValue());
        
        return jpaRepository.findByIdWithDeletedCheck(id.getValue())
            .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoldPreventionZone> findByTenantId(TenantId tenantId) {
        log.debug("查询租户下的防霉管控区域：tenantId={}", tenantId.getValue());
        
        List<MoldPreventionZoneEntity> entities = jpaRepository.findByTenantId(tenantId.getValue());
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoldPreventionZone> findBySpaceId(SpaceId spaceId) {
        log.debug("查询空间下的防霉管控区域：spaceId={}", spaceId.getValue());
        
        List<MoldPreventionZoneEntity> entities = jpaRepository.findBySpaceId(spaceId.getValue());
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoldPreventionZone> findActiveZones(TenantId tenantId) {
        log.debug("查询租户下的活跃防霉管控区域：tenantId={}", tenantId.getValue());
        
        List<MoldPreventionZoneEntity> entities = jpaRepository.findActiveZones(tenantId.getValue());
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(ZoneId id) {
        log.debug("删除防霉管控区域：id={}", id.getValue());
        
        jpaRepository.findByIdWithDeletedCheck(id.getValue())
            .ifPresent(entity -> {
                entity.setDeleted(true);
                entity.setDeletedAt(java.time.LocalDateTime.now());
                jpaRepository.save(entity);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code, TenantId tenantId) {
        log.debug("检查防霉管控区域编码是否存在：code={}, tenantId={}", code, tenantId.getValue());
        return jpaRepository.existsByZoneCodeAndTenantId(code, tenantId.getValue());
    }

    /**
     * 将 JPA 实体转换为领域模型
     * 注意：这是一个简化实现，实际项目中需要完整的转换逻辑
     */
    private MoldPreventionZone toDomain(MoldPreventionZoneEntity entity) {
        return MoldPreventionZone.builder()
            .id(ZoneId.of(entity.getId().toString()))
            .name(ZoneName.of(entity.getZoneName()))
            .code(ZoneCode.of(entity.getZoneCode()))
            .spaceId(SpaceId.of(entity.getSpaceId()))
            .tenantId(TenantId.of(entity.getTenantId()))
            .status(com.hkt.iot.smartapps.moldprevention.domain.model.ZoneStatus.valueOf(entity.getZoneStatus().name()))
            .currentRiskLevel(entity.getCurrentRiskLevel() != null 
                ? MoldRiskLevel.valueOf(entity.getCurrentRiskLevel()) 
                : MoldRiskLevel.LOW)
            .description(entity.getDescription())
            .sensors(new ArrayList<>())
            .controllers(new ArrayList<>())
            .version(entity.getVersion())
            .build();
    }
}
