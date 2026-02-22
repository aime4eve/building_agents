package com.hkt.iot.notification.domain.repository;

import com.hkt.iot.notification.domain.model.NotificationLog;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * 通知日志仓储接口
 *
 * @author HKT IoT Team
 */
@Repository
public interface NotificationLogRepository {

    /**
     * 保存日志
     */
    NotificationLog save(NotificationLog log);

    /**
     * 根据请求ID查找日志
     */
    List<NotificationLog> findByRequestId(Long requestId);

    /**
     * 根据租户ID查找日志（分页）
     */
    List<NotificationLog> findByTenantId(String tenantId, int page, int size);

    /**
     * 根据幂等键查找日志
     */
    List<NotificationLog> findByDedupeKey(String dedupeKey);

    /**
     * 根据CorrelationID查找日志
     */
    List<NotificationLog> findByCorrelationId(String correlationId);

    /**
     * 查找指定时间范围内的日志
     */
    List<NotificationLog> findByTimeRange(String tenantId, Instant start, Instant end);

    /**
     * 统计租户的通知数量
     */
    long countByTenantId(String tenantId);

    /**
     * 统计租户的成功通知数量
     */
    long countSuccessByTenantId(String tenantId);

    /**
     * 统计租户的失败通知数量
     */
    long countFailedByTenantId(String tenantId);

    /**
     * 删除过期日志
     */
    int deleteExpiredLogs(int days);

    /**
     * 批量保存日志
     */
    void batchSave(List<NotificationLog> logs);
}
