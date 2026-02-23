package com.hkt.iot.workflow.infrastructure.persistence.jpa;

import com.hkt.iot.workflow.infrastructure.persistence.po.ProcessInstancePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 流程实例 JPA Repository
 *
 * @author HKT IoT Team
 */
@Repository
public interface ProcessInstanceJpaRepository extends JpaRepository<ProcessInstancePO, String> {

    /**
     * 根据业务键查找
     */
    Optional<ProcessInstancePO> findByBusinessKey(String businessKey);

    /**
     * 根据租户 ID 查找
     */
    List<ProcessInstancePO> findByTenantId(String tenantId);

    /**
     * 根据状态查找
     */
    List<ProcessInstancePO> findByState(String state);

    /**
     * 根据租户 ID 和状态查找
     */
    List<ProcessInstancePO> findByTenantIdAndState(String tenantId, String state);

    /**
     * 检查业务键是否存在
     */
    boolean existsByBusinessKey(String businessKey);

    /**
     * 带乐观锁的查找
     */
    @Lock(jakarta.persistence.LockModeType.OPTIMISTIC)
    @Query("SELECT p FROM ProcessInstancePO p WHERE p.id = :id")
    Optional<ProcessInstancePO> findByIdWithLock(String id);
}
