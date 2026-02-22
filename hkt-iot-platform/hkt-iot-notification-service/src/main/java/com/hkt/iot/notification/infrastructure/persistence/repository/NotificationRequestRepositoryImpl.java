package com.hkt.iot.notification.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hkt.iot.notification.domain.model.NotificationRequest;
import com.hkt.iot.notification.domain.model.NotificationTemplate;
import com.hkt.iot.notification.domain.repository.NotificationRequestRepository;
import com.hkt.iot.notification.infrastructure.persistence.mapper.NotificationRequestMapper;
import com.hkt.iot.notification.infrastructure.persistence.po.NotificationRequestPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationRequestRepositoryImpl implements NotificationRequestRepository {

    private final NotificationRequestMapper notificationRequestMapper;

    @Override
    public NotificationRequest save(NotificationRequest request) {
        NotificationRequestPO po = toPO(request);
        if (request.getId() == null) {
            po.setCreatedAt(Instant.now());
            po.setUpdatedAt(Instant.now());
            notificationRequestMapper.insert(po);
            request.setId(po.getId());
        } else {
            po.setUpdatedAt(Instant.now());
            notificationRequestMapper.updateById(po);
        }
        log.debug("[NotificationRequestRepository] Saved request with id={}, dedupeKey={}", po.getId(), po.getDedupeKey());
        return request;
    }

    @Override
    public Optional<NotificationRequest> findById(Long id) {
        log.debug("[NotificationRequestRepository] Finding request by id={}", id);
        NotificationRequestPO po = notificationRequestMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<NotificationRequest> findByDedupeKey(String dedupeKey) {
        log.debug("[NotificationRequestRepository] Finding request by dedupeKey={}", dedupeKey);
        LambdaQueryWrapper<NotificationRequestPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationRequestPO::getDedupeKey, dedupeKey);
        NotificationRequestPO po = notificationRequestMapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<NotificationRequest> findPendingRequests(int limit) {
        log.debug("[NotificationRequestRepository] Finding pending requests with limit={}", limit);
        LambdaQueryWrapper<NotificationRequestPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationRequestPO::getStatus, NotificationRequest.NotificationStatus.PENDING.getCode())
                .and(w -> w.isNull(NotificationRequestPO::getScheduledAt)
                        .or()
                        .le(NotificationRequestPO::getScheduledAt, Instant.now()))
                .orderByAsc(NotificationRequestPO::getCreatedAt)
                .last("LIMIT " + limit);
        List<NotificationRequestPO> poList = notificationRequestMapper.selectList(wrapper);
        log.debug("[NotificationRequestRepository] Found {} pending requests", poList.size());
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<NotificationRequest> findRetryableRequests(int limit) {
        log.debug("[NotificationRequestRepository] Finding retryable requests with limit={}", limit);
        LambdaQueryWrapper<NotificationRequestPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationRequestPO::getStatus, NotificationRequest.NotificationStatus.PENDING.getCode())
                .lt(NotificationRequestPO::getRetryCount, NotificationRequestPO::getMaxRetry)
                .isNotNull(NotificationRequestPO::getNextRetryAt)
                .le(NotificationRequestPO::getNextRetryAt, Instant.now())
                .orderByAsc(NotificationRequestPO::getNextRetryAt)
                .last("LIMIT " + limit);
        List<NotificationRequestPO> poList = notificationRequestMapper.selectList(wrapper);
        log.debug("[NotificationRequestRepository] Found {} retryable requests", poList.size());
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<NotificationRequest> findByTenantIdAndBusinessId(String tenantId, String businessId) {
        log.debug("[NotificationRequestRepository] Finding requests by tenantId={}, businessId={}", tenantId, businessId);
        LambdaQueryWrapper<NotificationRequestPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationRequestPO::getTenantId, tenantId)
                .eq(NotificationRequestPO::getBusinessId, businessId)
                .orderByDesc(NotificationRequestPO::getCreatedAt);
        List<NotificationRequestPO> poList = notificationRequestMapper.selectList(wrapper);
        log.debug("[NotificationRequestRepository] Found {} requests for tenantId={}, businessId={}", poList.size(), tenantId, businessId);
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<NotificationRequest> findByCorrelationId(String correlationId) {
        log.debug("[NotificationRequestRepository] Finding requests by correlationId={}", correlationId);
        LambdaQueryWrapper<NotificationRequestPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationRequestPO::getCorrelationId, correlationId)
                .orderByDesc(NotificationRequestPO::getCreatedAt);
        List<NotificationRequestPO> poList = notificationRequestMapper.selectList(wrapper);
        log.debug("[NotificationRequestRepository] Found {} requests for correlationId={}", poList.size(), correlationId);
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        log.debug("[NotificationRequestRepository] Deleting request by id={}", id);
        notificationRequestMapper.deleteById(id);
    }

    @Override
    public int deleteExpiredRequests(int days) {
        log.debug("[NotificationRequestRepository] Deleting expired requests older than {} days", days);
        Instant cutoffTime = Instant.now().minus(days, ChronoUnit.DAYS);
        LambdaQueryWrapper<NotificationRequestPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(NotificationRequestPO::getCreatedAt, cutoffTime)
                .in(NotificationRequestPO::getStatus, 
                        NotificationRequest.NotificationStatus.SUCCESS.getCode(),
                        NotificationRequest.NotificationStatus.FAILED.getCode(),
                        NotificationRequest.NotificationStatus.CANCELLED.getCode());
        int deleted = notificationRequestMapper.delete(wrapper);
        log.debug("[NotificationRequestRepository] Deleted {} expired requests", deleted);
        return deleted;
    }

    @Override
    public long countByTenantId(String tenantId) {
        log.debug("[NotificationRequestRepository] Counting requests for tenantId={}", tenantId);
        LambdaQueryWrapper<NotificationRequestPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationRequestPO::getTenantId, tenantId);
        return notificationRequestMapper.selectCount(wrapper);
    }

    @Override
    public long countFailedByTenantId(String tenantId) {
        log.debug("[NotificationRequestRepository] Counting failed requests for tenantId={}", tenantId);
        LambdaQueryWrapper<NotificationRequestPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationRequestPO::getTenantId, tenantId)
                .eq(NotificationRequestPO::getStatus, NotificationRequest.NotificationStatus.FAILED.getCode());
        return notificationRequestMapper.selectCount(wrapper);
    }

    private NotificationRequestPO toPO(NotificationRequest domain) {
        return NotificationRequestPO.builder()
                .id(domain.getId())
                .dedupeKey(domain.getDedupeKey())
                .tenantId(domain.getTenantId())
                .channelType(domain.getChannelType() != null ? domain.getChannelType().name() : null)
                .receiverType(domain.getReceiverType() != null ? domain.getReceiverType().name() : null)
                .receiverId(domain.getReceiverId())
                .receiverAddress(domain.getReceiverAddress())
                .templateCode(domain.getTemplateCode())
                .title(domain.getTitle())
                .content(domain.getContent())
                .variables(domain.getVariables())
                .priority(domain.getPriority() != null ? domain.getPriority().name() : null)
                .status(domain.getStatus() != null ? domain.getStatus().name() : null)
                .retryCount(domain.getRetryCount())
                .maxRetry(domain.getMaxRetry())
                .nextRetryAt(domain.getNextRetryAt())
                .errorMessage(domain.getErrorMessage())
                .businessType(domain.getBusinessType())
                .businessId(domain.getBusinessId())
                .correlationId(domain.getCorrelationId())
                .scheduledAt(domain.getScheduledAt())
                .sentAt(domain.getSentAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private NotificationRequest toDomain(NotificationRequestPO po) {
        NotificationRequest domain = NotificationRequest.builder()
                .id(po.getId())
                .dedupeKey(po.getDedupeKey())
                .tenantId(po.getTenantId())
                .channelType(po.getChannelType() != null ? NotificationTemplate.ChannelType.valueOf(po.getChannelType()) : null)
                .receiverType(po.getReceiverType() != null ? NotificationRequest.ReceiverType.valueOf(po.getReceiverType()) : null)
                .receiverId(po.getReceiverId())
                .receiverAddress(po.getReceiverAddress())
                .templateCode(po.getTemplateCode())
                .title(po.getTitle())
                .content(po.getContent())
                .variables(po.getVariables())
                .priority(po.getPriority() != null ? NotificationRequest.Priority.valueOf(po.getPriority()) : null)
                .status(po.getStatus() != null ? NotificationRequest.NotificationStatus.valueOf(po.getStatus()) : null)
                .retryCount(po.getRetryCount())
                .maxRetry(po.getMaxRetry())
                .nextRetryAt(po.getNextRetryAt())
                .errorMessage(po.getErrorMessage())
                .businessType(po.getBusinessType())
                .businessId(po.getBusinessId())
                .correlationId(po.getCorrelationId())
                .scheduledAt(po.getScheduledAt())
                .sentAt(po.getSentAt())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
        return domain;
    }
}
