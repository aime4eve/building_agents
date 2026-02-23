package com.hkt.iot.workflow.infrastructure.persistence.jpa;

import com.hkt.iot.workflow.infrastructure.persistence.po.ProcessVariablePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 流程变量JPA仓储
 */
@Repository
public interface ProcessVariableJpaRepository extends JpaRepository<ProcessVariablePO, String> {

    List<ProcessVariablePO> findByProcessInstanceId(String processInstanceId);

    Optional<ProcessVariablePO> findByProcessInstanceIdAndName(String processInstanceId, String name);

    void deleteByProcessInstanceId(String processInstanceId);
}
