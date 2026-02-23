package com.hkt.iot.workflow.infrastructure.persistence.jpa;

import com.hkt.iot.workflow.infrastructure.persistence.po.ProcessHistoryPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 流程历史JPA仓储
 */
@Repository
public interface ProcessHistoryJpaRepository extends JpaRepository<ProcessHistoryPO, String> {

    List<ProcessHistoryPO> findByProcessInstanceId(String processInstanceId);

    List<ProcessHistoryPO> findByProcessInstanceIdAndType(String processInstanceId, String type);

    List<ProcessHistoryPO> findByTaskId(String taskId);
}
