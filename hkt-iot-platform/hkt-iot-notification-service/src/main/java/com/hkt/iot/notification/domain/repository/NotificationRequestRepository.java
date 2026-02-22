package com.hkt.iot.notification.domain.repository;

import com.hkt.iot.notification.domain.model.NotificationRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 通知请求仓储接口
 *
 * @author HKT IoT Team
 */
@Repository
public interface NotificationRequestRepository {

    /**
     * 保存请求
     */
    NotificationRequest save(NotificationRequest request);

    /**
     * 根据ID查找请求
     */
    Optional<NotificationRequest> findById(Long id);

    /**
     * 根据幂等键查找请求
     */
    Optional<NotificationRequest> findByDedupeKey(String dedupeKey);

    /**
     * 查找待发送的请求
     */
    List<NotificationRequest> findPendingRequests(int limit);

    /**
     * 查找可重试的请求
     */
    List<NotificationRequest> findRetryableRequests(int limit);

    /**
     * 根据租户ID和业务ID查找请求
     */
    List<NotificationRequest> findByTenantIdAndBusinessId(String tenantId, String businessId);

    /**
     * 根据CorrelationID查找请求
     */
    List<NotificationRequest> findByCorrelationId(String correlationId);

    /**
     * 删除请求
     */
    void deleteById(Long id);

    /**
     * 批量删除过期请求
     */
    int deleteExpiredRequests(int days);

    /**
     * 统计租户的通知请求数量
     */
    long countByTenantId(String tenantId);

    /**
     * 统计租户的失败请求数量
     */
    long countFailedByTenantId(String tenantId);
}
