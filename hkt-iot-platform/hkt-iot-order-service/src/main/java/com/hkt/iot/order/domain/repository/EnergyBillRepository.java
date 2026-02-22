package com.hkt.iot.order.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.order.domain.model.BillStatus;
import com.hkt.iot.order.domain.model.EnergyBill;
import com.hkt.iot.order.domain.model.EnergyType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 能耗账单仓储接口
 *
 * @author HKT IoT Team
 */
public interface EnergyBillRepository extends BaseRepository<EnergyBill, Long> {

    /**
     * 根据账单编号查找
     */
    Optional<EnergyBill> findByBillNo(String billNo);

    /**
     * 根据租户ID查找账单
     */
    List<EnergyBill> findByTenantId(Long tenantId);

    /**
     * 根据空间ID查找账单
     */
    List<EnergyBill> findBySpaceId(Long spaceId);

    /**
     * 根据账单状态查找
     */
    List<EnergyBill> findByBillStatus(BillStatus billStatus);

    /**
     * 根据能耗类型查找
     */
    List<EnergyBill> findByEnergyType(EnergyType energyType);

    /**
     * 查找指定账单周期的账单
     */
    List<EnergyBill> findByBillingYearAndBillingMonth(Integer year, Integer month);

    /**
     * 查找租户指定账单周期的账单
     */
    List<EnergyBill> findByTenantIdAndBillingYearAndBillingMonth(Long tenantId, Integer year, Integer month);

    /**
     * 查找租户指定账单周期和能耗类型的账单
     */
    Optional<EnergyBill> findByTenantIdAndBillingYearAndBillingMonthAndEnergyType(
            Long tenantId, Integer year, Integer month, EnergyType energyType);

    /**
     * 查找空间指定账单周期和能耗类型的账单
     */
    Optional<EnergyBill> findBySpaceIdAndBillingYearAndBillingMonthAndEnergyType(
            Long spaceId, Integer year, Integer month, EnergyType energyType);

    /**
     * 检查账单编号是否存在
     */
    boolean existsByBillNo(String billNo);

    /**
     * 检查空间指定周期账单是否已存在
     */
    boolean existsBySpaceIdAndBillingYearAndBillingMonthAndEnergyType(
            Long spaceId, Integer year, Integer month, EnergyType energyType);

    /**
     * 统计租户账单数量
     */
    long countByTenantId(Long tenantId);

    /**
     * 统计租户未支付账单数量
     */
    long countByTenantIdAndBillStatus(Long tenantId, BillStatus billStatus);

    /**
     * 查找租户指定日期范围内的账单
     */
    List<EnergyBill> findByTenantIdAndStartDateBetween(Long tenantId, LocalDate start, LocalDate end);

    /**
     * 根据关联订单ID查找账单
     */
    Optional<EnergyBill> findByOrderId(Long orderId);
}
