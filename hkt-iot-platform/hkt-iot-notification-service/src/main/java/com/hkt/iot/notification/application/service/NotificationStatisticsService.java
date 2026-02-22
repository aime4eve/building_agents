package com.hkt.iot.notification.application.service;

import com.hkt.iot.notification.application.dto.NotificationStatisticsDTO;
import com.hkt.iot.notification.domain.model.NotificationLog;
import com.hkt.iot.notification.domain.model.NotificationRequest;
import com.hkt.iot.notification.domain.model.NotificationTemplate;
import com.hkt.iot.notification.domain.repository.NotificationLogRepository;
import com.hkt.iot.notification.domain.repository.NotificationRequestRepository;
import com.hkt.iot.notification.domain.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知统计服务
 * 提供通知发送的统计分析功能
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationStatisticsService {

    private final NotificationLogRepository logRepository;
    private final NotificationRequestRepository requestRepository;
    private final NotificationTemplateRepository templateRepository;

    public NotificationStatisticsDTO getDailyStatistics(String tenantId, Instant date) {
        log.info("获取每日统计, tenantId={}, date={}", tenantId, date);
        
        Instant targetDate = date != null ? date : Instant.now();
        NotificationStatisticsDTO.TimeRange timeRange = NotificationStatisticsDTO.TimeRange.daily(targetDate);
        
        return buildStatistics(tenantId, timeRange, true, true, true);
    }

    public NotificationStatisticsDTO getWeeklyStatistics(String tenantId, Instant date) {
        log.info("获取每周统计, tenantId={}, date={}", tenantId, date);
        
        Instant targetDate = date != null ? date : Instant.now();
        NotificationStatisticsDTO.TimeRange timeRange = NotificationStatisticsDTO.TimeRange.weekly(targetDate);
        
        return buildStatistics(tenantId, timeRange, true, true, true);
    }

    public NotificationStatisticsDTO getMonthlyStatistics(String tenantId, Instant date) {
        log.info("获取每月统计, tenantId={}, date={}", tenantId, date);
        
        Instant targetDate = date != null ? date : Instant.now();
        NotificationStatisticsDTO.TimeRange timeRange = NotificationStatisticsDTO.TimeRange.monthly(targetDate);
        
        return buildStatistics(tenantId, timeRange, true, true, true);
    }

    public NotificationStatisticsDTO getCustomRangeStatistics(String tenantId, Instant startTime, Instant endTime) {
        log.info("获取自定义时间范围统计, tenantId={}, startTime={}, endTime={}", tenantId, startTime, endTime);
        
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("开始时间和结束时间不能为空");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        
        NotificationStatisticsDTO.TimeRange timeRange = NotificationStatisticsDTO.TimeRange.custom(startTime, endTime);
        
        return buildStatistics(tenantId, timeRange, true, true, true);
    }

    public NotificationStatisticsDTO getStatisticsByChannel(String tenantId, Instant startTime, Instant endTime) {
        log.info("按渠道获取统计, tenantId={}, startTime={}, endTime={}", tenantId, startTime, endTime);
        
        NotificationStatisticsDTO.TimeRange timeRange;
        if (startTime != null && endTime != null) {
            timeRange = NotificationStatisticsDTO.TimeRange.custom(startTime, endTime);
        } else {
            Instant now = Instant.now();
            timeRange = NotificationStatisticsDTO.TimeRange.monthly(now);
        }
        
        return buildStatistics(tenantId, timeRange, true, false, false);
    }

    public NotificationStatisticsDTO getStatisticsByTemplate(String tenantId, Instant startTime, Instant endTime) {
        log.info("按模板获取统计, tenantId={}, startTime={}, endTime={}", tenantId, startTime, endTime);
        
        NotificationStatisticsDTO.TimeRange timeRange;
        if (startTime != null && endTime != null) {
            timeRange = NotificationStatisticsDTO.TimeRange.custom(startTime, endTime);
        } else {
            Instant now = Instant.now();
            timeRange = NotificationStatisticsDTO.TimeRange.monthly(now);
        }
        
        return buildStatistics(tenantId, timeRange, false, true, false);
    }

    private NotificationStatisticsDTO buildStatistics(String tenantId, 
                                                       NotificationStatisticsDTO.TimeRange timeRange,
                                                       boolean includeChannelStats,
                                                       boolean includeTemplateStats,
                                                       boolean includeDailyStats) {
        Instant start = timeRange.getStartInstant();
        Instant end = timeRange.getEndInstant();
        
        List<NotificationLog> logs = logRepository.findByTimeRange(tenantId, start, end);
        log.debug("查询到日志数量: {}", logs.size());
        
        long totalCount = logs.size();
        long successCount = logs.stream()
                .filter(l -> l.getStatus() == NotificationRequest.NotificationStatus.SUCCESS)
                .count();
        long failedCount = logs.stream()
                .filter(l -> l.getStatus() == NotificationRequest.NotificationStatus.FAILED)
                .count();
        long pendingCount = logs.stream()
                .filter(l -> l.getStatus() == NotificationRequest.NotificationStatus.PENDING ||
                        l.getStatus() == NotificationRequest.NotificationStatus.SENDING)
                .count();
        
        NotificationStatisticsDTO.NotificationStatisticsDTOBuilder builder = NotificationStatisticsDTO.builder()
                .timeRange(timeRange)
                .totalCount(totalCount)
                .successCount(successCount)
                .failedCount(failedCount)
                .pendingCount(pendingCount);
        
        if (includeChannelStats) {
            builder.channelStatistics(buildChannelStatistics(logs));
        }
        
        if (includeTemplateStats) {
            builder.templateStatistics(buildTemplateStatistics(logs, tenantId));
        }
        
        if (includeDailyStats) {
            builder.dailyStatistics(buildDailyStatistics(logs, start, end));
        }
        
        NotificationStatisticsDTO result = builder.build();
        result.calculateSuccessRate();
        result.calculateChannelSuccessRate();
        result.calculateTemplateSuccessRate();
        result.calculateDailySuccessRate();
        
        return result;
    }

    private List<NotificationStatisticsDTO.ChannelStatistics> buildChannelStatistics(List<NotificationLog> logs) {
        Map<NotificationTemplate.ChannelType, List<NotificationLog>> groupedByChannel = new HashMap<>();
        
        for (NotificationLog log : logs) {
            NotificationTemplate.ChannelType channelType = log.getChannelType();
            groupedByChannel.computeIfAbsent(channelType, k -> new ArrayList<>()).add(log);
        }
        
        List<NotificationStatisticsDTO.ChannelStatistics> channelStats = new ArrayList<>();
        
        for (Map.Entry<NotificationTemplate.ChannelType, List<NotificationLog>> entry : groupedByChannel.entrySet()) {
            NotificationTemplate.ChannelType channelType = entry.getKey();
            List<NotificationLog> channelLogs = entry.getValue();
            
            long total = channelLogs.size();
            long success = channelLogs.stream()
                    .filter(l -> l.getStatus() == NotificationRequest.NotificationStatus.SUCCESS)
                    .count();
            long failed = channelLogs.stream()
                    .filter(l -> l.getStatus() == NotificationRequest.NotificationStatus.FAILED)
                    .count();
            long pending = channelLogs.stream()
                    .filter(l -> l.getStatus() == NotificationRequest.NotificationStatus.PENDING ||
                            l.getStatus() == NotificationRequest.NotificationStatus.SENDING)
                    .count();
            
            channelStats.add(NotificationStatisticsDTO.ChannelStatistics.builder()
                    .channelType(channelType.name())
                    .channelName(channelType.getDescription())
                    .totalCount(total)
                    .successCount(success)
                    .failedCount(failed)
                    .pendingCount(pending)
                    .build());
        }
        
        return channelStats;
    }

    private List<NotificationStatisticsDTO.TemplateStatistics> buildTemplateStatistics(
            List<NotificationLog> logs, String tenantId) {
        Map<String, List<NotificationLog>> groupedByTemplate = new HashMap<>();
        
        List<NotificationRequest> requests = new ArrayList<>();
        for (NotificationLog log : logs) {
            if (log.getRequestId() != null) {
                Optional<NotificationRequest> requestOpt = requestRepository.findById(log.getRequestId());
                requestOpt.ifPresent(requests::add);
            }
        }
        
        Map<Long, String> requestIdToTemplateCode = new HashMap<>();
        for (NotificationRequest request : requests) {
            requestIdToTemplateCode.put(request.getId(), request.getTemplateCode());
        }
        
        for (NotificationLog log : logs) {
            String templateCode = requestIdToTemplateCode.get(log.getRequestId());
            if (templateCode != null) {
                groupedByTemplate.computeIfAbsent(templateCode, k -> new ArrayList<>()).add(log);
            }
        }
        
        List<NotificationStatisticsDTO.TemplateStatistics> templateStats = new ArrayList<>();
        
        for (Map.Entry<String, List<NotificationLog>> entry : groupedByTemplate.entrySet()) {
            String templateCode = entry.getKey();
            List<NotificationLog> templateLogs = entry.getValue();
            
            String templateName = getTemplateName(templateCode, tenantId);
            
            long total = templateLogs.size();
            long success = templateLogs.stream()
                    .filter(l -> l.getStatus() == NotificationRequest.NotificationStatus.SUCCESS)
                    .count();
            long failed = templateLogs.stream()
                    .filter(l -> l.getStatus() == NotificationRequest.NotificationStatus.FAILED)
                    .count();
            long pending = templateLogs.stream()
                    .filter(l -> l.getStatus() == NotificationRequest.NotificationStatus.PENDING ||
                            l.getStatus() == NotificationRequest.NotificationStatus.SENDING)
                    .count();
            
            templateStats.add(NotificationStatisticsDTO.TemplateStatistics.builder()
                    .templateCode(templateCode)
                    .templateName(templateName)
                    .totalCount(total)
                    .successCount(success)
                    .failedCount(failed)
                    .pendingCount(pending)
                    .build());
        }
        
        return templateStats;
    }

    private String getTemplateName(String templateCode, String tenantId) {
        Optional<NotificationTemplate> templateOpt = templateRepository.findByTenantIdAndTemplateCode(tenantId, templateCode);
        if (templateOpt.isEmpty()) {
            templateOpt = templateRepository.findByTemplateCode(templateCode);
        }
        return templateOpt.map(NotificationTemplate::getTemplateName).orElse(templateCode);
    }

    private List<NotificationStatisticsDTO.DailyStatistics> buildDailyStatistics(
            List<NotificationLog> logs, Instant start, Instant end) {
        
        Map<LocalDate, List<NotificationLog>> groupedByDate = new HashMap<>();
        
        for (NotificationLog log : logs) {
            if (log.getCreatedAt() != null) {
                LocalDate date = log.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate();
                groupedByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(log);
            }
        }
        
        List<NotificationStatisticsDTO.DailyStatistics> dailyStats = new ArrayList<>();
        
        LocalDate startDate = start.atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate endDate = end.atZone(ZoneOffset.UTC).toLocalDate();
        
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            List<NotificationLog> dayLogs = groupedByDate.getOrDefault(date, new ArrayList<>());
            
            long total = dayLogs.size();
            long success = dayLogs.stream()
                    .filter(l -> l.getStatus() == NotificationRequest.NotificationStatus.SUCCESS)
                    .count();
            long failed = dayLogs.stream()
                    .filter(l -> l.getStatus() == NotificationRequest.NotificationStatus.FAILED)
                    .count();
            long pending = dayLogs.stream()
                    .filter(l -> l.getStatus() == NotificationRequest.NotificationStatus.PENDING ||
                            l.getStatus() == NotificationRequest.NotificationStatus.SENDING)
                    .count();
            
            dailyStats.add(NotificationStatisticsDTO.DailyStatistics.builder()
                    .date(date.atStartOfDay(ZoneOffset.UTC).toInstant().getEpochSecond())
                    .totalCount(total)
                    .successCount(success)
                    .failedCount(failed)
                    .pendingCount(pending)
                    .build());
        }
        
        return dailyStats;
    }
}
