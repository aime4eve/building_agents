package com.hkt.iot.scene.domain.repository;

import com.hkt.iot.domain.shared.SpaceId;
import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.scene.domain.model.Schedule;
import com.hkt.iot.scene.domain.model.ScheduleCode;
import com.hkt.iot.scene.domain.model.ScheduleId;
import com.hkt.iot.scene.domain.model.ScheduleStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 定时计划仓储接口
 *
 * 职责：管理定时计划聚合根的持久化
 */
public interface ScheduleRepository {

    /**
     * 保存定时计划
     */
    Schedule save(Schedule schedule);

    /**
     * 根据ID查找定时计划
     */
    Optional<Schedule> findById(ScheduleId id);

    /**
     * 根据编码查找定时计划
     */
    Optional<Schedule> findByCode(ScheduleCode code);

    /**
     * 根据租户查找定时计划列表
     */
    List<Schedule> findByTenant(TenantId tenantId);

    /**
     * 根据租户和状态查找定时计划列表
     */
    List<Schedule> findByTenantAndStatus(TenantId tenantId, ScheduleStatus status);

    /**
     * 根据空间查找定时计划列表
     */
    List<Schedule> findBySpace(SpaceId spaceId);

    /**
     * 查找租户下激活的定时计划列表
     */
    List<Schedule> findActiveSchedules(TenantId tenantId);

    /**
     * 查找待执行的定时计划列表（下次执行时间早于指定时间）
     */
    List<Schedule> findPendingSchedules(LocalDateTime beforeTime);

    /**
     * 查找需要执行的定时计划列表（下次执行时间已到且在有效期内）
     */
    List<Schedule> findDueSchedules(LocalDateTime now);

    /**
     * 删除定时计划
     */
    void delete(Schedule schedule);

    /**
     * 根据ID删除定时计划
     */
    void deleteById(ScheduleId id);

    /**
     * 检查编码是否存在
     */
    boolean existsByCode(ScheduleCode code);

    /**
     * 检查编码是否存在（排除指定ID）
     */
    boolean existsByCodeAndIdNot(ScheduleCode code, ScheduleId id);
}
