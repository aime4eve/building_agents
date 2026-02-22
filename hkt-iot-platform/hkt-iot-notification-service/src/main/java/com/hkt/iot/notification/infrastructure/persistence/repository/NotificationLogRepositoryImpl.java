package com.hkt.iot.notification.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hkt.iot.notification.domain.model.NotificationLog;
import com.hkt.iot.notification.domain.model.NotificationRequest;
import com.hkt.iot.notification.domain.model.NotificationTemplate;
import com.hkt.iot.notification.domain.repository.NotificationLogRepository;
import com.hkt.iot.notification.infrastructure.persistence.mapper.NotificationLogMapper;
import com.hkt.iot.notification.infrastructure.persistence.po.NotificationLogPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationLogRepositoryImpl implements NotificationLogRepository {

    private final NotificationLogMapper notificationLogMapper;

    @Override
    public NotificationLog save(NotificationLog logEntity) {
        NotificationLogPO po = toPO(logEntity);
        if (logEntity.getId() == null) {
            po.setCreatedAt(Instant.now());
            notificationLogMapper.insert(po);
            logEntity.setId(po.getId());
        } else {
            notificationLogMapper.updateById(po);
        }
        log.debug("[NotificationLogRepository] Saved log with id={}, requestId={}", po.getId(), po.getRequestId());
        return logEntity;
    }

    @Override
    public List<NotificationLog> findByRequestId(Long requestId) {
        log.debug("[NotificationLogRepository] Finding logs by requestId={}", requestId);
        LambdaQueryWrapper<NotificationLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationLogPO::getRequestId, requestId)
                .orderByDesc(NotificationLogPO::getCreatedAt);
        List<NotificationLogPO> poList = notificationLogMapper.selectList(wrapper);
        log.debug("[NotificationLogRepository] Found {} logs for requestId={}", poList.size(), requestId);
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<NotificationLog> findByTenantId(String tenantId, int page, int size) {
        log.debug("[NotificationLogRepository] Finding logs by tenantId={}, page={}, size={}", tenantId, page, size);
        int offset = page * size;
        LambdaQueryWrapper<NotificationLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationLogPO::getTenantId, tenantId)
                .orderByDesc(NotificationLogPO::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + offset);
        List<NotificationLogPO> poList = notificationLogMapper.selectList(wrapper);
        log.debug("[NotificationLogRepository] Found {} logs for tenantId={}", poList.size(), tenantId);
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<NotificationLog> findByDedupeKey(String dedupeKey) {
        log.debug("[NotificationLogRepository] Finding logs by dedupeKey={}", dedupeKey);
        LambdaQueryWrapper<NotificationLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationLogPO::getDedupeKey, dedupeKey)
                .orderByDesc(NotificationLogPO::getCreatedAt);
        List<NotificationLogPO> poList = notificationLogMapper.selectList(wrapper);
        log.debug("[NotificationLogRepository] Found {} logs for dedupeKey={}", poList.size(), dedupeKey);
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<NotificationLog> findByCorrelationId(String correlationId) {
        log.debug("[NotificationLogRepository] Finding logs by correlationId={}", correlationId);
        LambdaQueryWrapper<NotificationLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationLogPO::getCorrelationId, correlationId)
                .orderByDesc(NotificationLogPO::getCreatedAt);
        List<NotificationLogPO> poList = notificationLogMapper.selectList(wrapper);
        log.debug("[NotificationLogRepository] Found {} logs for correlationId={}", poList.size(), correlationId);
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<NotificationLog> findByTimeRange(String tenantId, Instant start, Instant end) {
        log.debug("[NotificationLogRepository] Finding logs by tenantId={}, timeRange=[{}, {}]", tenantId, start, end);
        LambdaQueryWrapper<NotificationLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationLogPO::getTenantId, tenantId)
                .ge(NotificationLogPO::getCreatedAt, start)
                .le(NotificationLogPO::getCreatedAt, end)
                .orderByDesc(NotificationLogPO::getCreatedAt);
        List<NotificationLogPO> poList = notificationLogMapper.selectList(wrapper);
        log.debug("[NotificationLogRepository] Found {} logs for tenantId={} in timeRange", poList.size(), tenantId);
        return poList.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countByTenantId(String tenantId) {
        log.debug("[NotificationLogRepository] Counting logs for tenantId={}", tenantId);
        LambdaQueryWrapper<NotificationLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationLogPO::getTenantId, tenantId);
        return notificationLogMapper.selectCount(wrapper);
    }

    @Override
    public long countSuccessByTenantId(String tenantId) {
        log.debug("[NotificationLogRepository] Counting success logs for tenantId={}", tenantId);
        LambdaQueryWrapper<NotificationLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationLogPO::getTenantId, tenantId)
                .eq(NotificationLogPO::getStatus, NotificationRequest.NotificationStatus.SUCCESS.getCode());
        return notificationLogMapper.selectCount(wrapper);
    }

    @Override
    public long countFailedByTenantId(String tenantId) {
        log.debug("[NotificationLogRepository] Counting failed logs for tenantId={}", tenantId);
        LambdaQueryWrapper<NotificationLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationLogPO::getTenantId, tenantId)
                .eq(NotificationLogPO::getStatus, NotificationRequest.NotificationStatus.FAILED.getCode());
        return notificationLogMapper.selectCount(wrapper);
    }

    @Override
    public int deleteExpiredLogs(int days) {
        log.debug("[NotificationLogRepository] Deleting expired logs older than {} days", days);
        Instant cutoffTime = Instant.now().minus(days, ChronoUnit.DAYS);
        LambdaQueryWrapper<NotificationLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(NotificationLogPO::getCreatedAt, cutoffTime);
        int deleted = notificationLogMapper.delete(wrapper);
        log.debug("[NotificationLogRepository] Deleted {} expired logs", deleted);
        return deleted;
    }

    @Override
    public void batchSave(List<NotificationLog> logs) {
        log.debug("[NotificationLogRepository] Batch saving {} logs", logs.size());
        List<NotificationLogPO> poList = logs.stream()
                .map(logEntity -> {
                    NotificationLogPO po = toPO(logEntity);
                    po.setCreatedAt(Instant.now());
                    return po;
                })
                .collect(Collectors.toList());
        for (NotificationLogPO po : poList) {
            notificationLogMapper.insert(po);
        }
        log.debug("[NotificationLogRepository] Batch saved {} logs", poList.size());
    }

    @Override
    public long countByTenantIdAndStatus(String tenantId, String status) {
        log.debug("[NotificationLogRepository] Counting logs for tenantId={}, status={}", tenantId, status);
        LambdaQueryWrapper<NotificationLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationLogPO::getTenantId, tenantId)
                .eq(NotificationLogPO::getStatus, status);
        return notificationLogMapper.selectCount(wrapper);
    }

    @Override
    public long countByTenantIdAndDateRange(String tenantId, Instant start, Instant end) {
        log.debug("[NotificationLogRepository] Counting logs for tenantId={}, dateRange=[{}, {}]", tenantId, start, end);
        LambdaQueryWrapper<NotificationLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationLogPO::getTenantId, tenantId)
                .ge(NotificationLogPO::getCreatedAt, start)
                .le(NotificationLogPO::getCreatedAt, end);
        return notificationLogMapper.selectCount(wrapper);
    }

    private NotificationLogPO toPO(NotificationLog domain) {
        return NotificationLogPO.builder()
                .id(domain.getId())
                .requestId(domain.getRequestId())
                .dedupeKey(domain.getDedupeKey())
                .tenantId(domain.getTenantId())
                .channelType(domain.getChannelType() != null ? domain.getChannelType().name() : null)
                .receiverType(domain.getReceiverType() != null ? domain.getReceiverType().name() : null)
                .receiverId(domain.getReceiverId())
                .receiverAddress(domain.getReceiverAddress())
                .title(domain.getTitle())
                .contentSummary(domain.getContentSummary())
                .status(domain.getStatus() != null ? domain.getStatus().name() : null)
                .responseCode(domain.getResponseCode())
                .responseMessage(domain.getResponseMessage())
                .externalMessageId(domain.getExternalMessageId())
                .correlationId(domain.getCorrelationId())
                .sentAt(domain.getSentAt())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    private NotificationLog toDomain(NotificationLogPO po) {
        return NotificationLog.builder()
                .id(po.getId())
                .requestId(po.getRequestId())
                .dedupeKey(po.getDedupeKey())
                .tenantId(po.getTenantId())
                .channelType(po.getChannelType() != null ? NotificationTemplate.ChannelType.valueOf(po.getChannelType()) : null)
                .receiverType(po.getReceiverType() != null ? NotificationRequest.ReceiverType.valueOf(po.getReceiverType()) : null)
                .receiverId(po.getReceiverId())
                .receiverAddress(po.getReceiverAddress())
                .title(po.getTitle())
                .contentSummary(po.getContentSummary())
                .status(po.getStatus() != null ? NotificationRequest.NotificationStatus.valueOf(po.getStatus()) : null)
                .responseCode(po.getResponseCode())
                .responseMessage(po.getResponseMessage())
                .externalMessageId(po.getExternalMessageId())
                .correlationId(po.getCorrelationId())
                .sentAt(po.getSentAt())
                .createdAt(po.getCreatedAt())
                .build();
    }
}
