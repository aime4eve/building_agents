package com.huakuantong.iot.platform.infrastructure.timeseries.influxdb;

import com.huakuantong.iot.platform.device.domain.repository.DeviceTelemetryReadRepository;
import com.huakuantong.iot.platform.shared.domain.PageRequest;
import com.huakuantong.iot.platform.shared.domain.PageResult;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * InfluxDB 设备遥测数据仓储实现
 *
 * <p>基于 InfluxDB 2.x Flux 查询语言实现</p>
 *
 * @author DDD Team
 * @version 1.0
 */
@Slf4j
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "timeseries.influx", name = "enabled", havingValue = "true")
public class InfluxDBDeviceTelemetryRepository implements DeviceTelemetryReadRepository {

    private final com.influxdb.client.InfluxDBClient influxDBClient;
    private final String org;
    private final String bucket;

    private static final String MEASUREMENT_TELEMETRY = "device_telemetry";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public Optional<DeviceTelemetry> findLatestByDeviceId(String deviceId) {
        String flux = buildLatestQuery(deviceId);

        List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, org);

        if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToTelemetry(tables.get(0).getRecords().get(0)));
    }

    @Override
    public Optional<DeviceTelemetry> findByDeviceIdAndTimestamp(String deviceId, LocalDateTime timestamp) {
        String flux = String.format(
            "from(bucket: \"%s\") " +
            "|> range(start: %s, stop: %s) " +
            "|> filter(fn: (r) => r._measurement == \"%s\") " +
            "|> filter(fn: (r) => r.device_id == \"%s\") " +
            "|> limit(n: 1)",
            bucket,
            timestamp.minusSeconds(1).format(FORMATTER),
            timestamp.plusSeconds(1).format(FORMATTER),
            MEASUREMENT_TELEMETRY,
            deviceId
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, org);

        if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToTelemetry(tables.get(0).getRecords().get(0)));
    }

    @Override
    public List<DeviceTelemetry> findByDeviceIdAndTimeRange(
        String deviceId,
        LocalDateTime from,
        LocalDateTime to
    ) {
        String flux = buildTimeRangeQuery(deviceId, from, to);

        List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, org);

        return tables.stream()
            .flatMap(table -> table.getRecords().stream())
            .map(this::mapToTelemetry)
            .collect(Collectors.toList());
    }

    @Override
    public PageResult<DeviceTelemetry> findByDeviceIdAndTimeRange(
        String deviceId,
        LocalDateTime from,
        LocalDateTime to,
        PageRequest pageRequest
    ) {
        String flux = buildTimeRangeQuery(deviceId, from, to) +
            String.format("|> limit(n: %d, offset: %d)",
                pageRequest.getPageSize(),
                (pageRequest.getPageNumber() - 1) * pageRequest.getPageSize());

        List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, org);

        List<DeviceTelemetry> data = tables.stream()
            .flatMap(table -> table.getRecords().stream())
            .map(this::mapToTelemetry)
            .collect(Collectors.toList());

        // TODO: 实现总数查询
        long total = data.size();

        return new PageResult<>(
            pageRequest.getPageNumber(),
            pageRequest.getPageSize(),
            total,
            data
        );
    }

    @Override
    public Optional<Double> findLatestMetricValue(String deviceId, String metricName) {
        String flux = String.format(
            "from(bucket: \"%s\") " +
            "|> range(start: -1h) " +
            "|> filter(fn: (r) => r._measurement == \"%s\") " +
            "|> filter(fn: (r) => r.device_id == \"%s\") " +
            "|> filter(fn: (r) => r._field == \"%s\") " +
            "|> last()",
            bucket, MEASUREMENT_TELEMETRY, deviceId, metricName
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, org);

        if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
            return Optional.empty();
        }

        Object value = tables.get(0).getRecords().get(0).getValue();
        return value instanceof Number ? Optional.of(((Number) value).doubleValue()) : Optional.empty();
    }

    @Override
    public List<MetricPoint> findMetricByTimeRange(
        String deviceId,
        String metricName,
        LocalDateTime from,
        LocalDateTime to
    ) {
        String flux = String.format(
            "from(bucket: \"%s\") " +
            "|> range(start: %s, stop: %s) " +
            "|> filter(fn: (r) => r._measurement == \"%s\") " +
            "|> filter(fn: (r) => r.device_id == \"%s\") " +
            "|> filter(fn: (r) => r._field == \"%s\")",
            bucket,
            from.format(FORMATTER),
            to.format(FORMATTER),
            MEASUREMENT_TELEMETRY,
            deviceId,
            metricName
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, org);

        return tables.stream()
            .flatMap(table -> table.getRecords().stream())
            .map(record -> new MetricPoint(
                LocalDateTime.ofInstant(
                    Instant.parse(record.getTime().toString()),
                    ZoneId.systemDefault()
                ),
                record.getValue() instanceof Number ? ((Number) record.getValue()).doubleValue() : null
            ))
            .collect(Collectors.toList());
    }

    @Override
    public List<DeviceTelemetry> findLatestByDeviceIds(List<String> deviceIds) {
        String deviceIdFilter = deviceIds.stream()
            .map(id -> String.format("r.device_id == \"%s\"", id))
            .collect(Collectors.joining(" or "));

        String flux = String.format(
            "from(bucket: \"%s\") " +
            "|> range(start: -5m) " +
            "|> filter(fn: (r) => r._measurement == \"%s\") " +
            "|> filter(fn: (r) => %s) " +
            "|> group(columns: [\"device_id\"]) " +
            "|> last()",
            bucket, MEASUREMENT_TELEMETRY, deviceIdFilter
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, org);

        return tables.stream()
            .filter(table -> !table.getRecords().isEmpty())
            .map(table -> mapToTelemetry(table.getRecords().get(0)))
            .collect(Collectors.toList());
    }

    @Override
    public List<DeviceTelemetry> findLatestBySpaceId(String spaceId) {
        String flux = String.format(
            "from(bucket: \"%s\") " +
            "|> range(start: -5m) " +
            "|> filter(fn: (r) => r._measurement == \"%s\") " +
            "|> filter(fn: (r) => r.space_id == \"%s\") " +
            "|> group(columns: [\"device_id\"]) " +
            "|> last()",
            bucket, MEASUREMENT_TELEMETRY, spaceId
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, org);

        return tables.stream()
            .filter(table -> !table.getRecords().isEmpty())
            .map(table -> mapToTelemetry(table.getRecords().get(0)))
            .collect(Collectors.toList());
    }

    @Override
    public List<DeviceTelemetry> findLatestByTenantId(String tenantId) {
        String flux = String.format(
            "from(bucket: \"%s\") " +
            "|> range(start: -5m) " +
            "|> filter(fn: (r) => r._measurement == \"%s\") " +
            "|> filter(fn: (r) => r.tenant_id == \"%s\") " +
            "|> group(columns: [\"device_id\"]) " +
            "|> last()",
            bucket, MEASUREMENT_TELEMETRY, tenantId
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, org);

        return tables.stream()
            .filter(table -> !table.getRecords().isEmpty())
            .map(table -> mapToTelemetry(table.getRecords().get(0)))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Double> aggregateMetric(
        String deviceId,
        String metricName,
        LocalDateTime from,
        LocalDateTime to,
        AggregateType aggregate
    ) {
        String fluxFunction = mapAggregateType(aggregate);

        String flux = String.format(
            "from(bucket: \"%s\") " +
            "|> range(start: %s, stop: %s) " +
            "|> filter(fn: (r) => r._measurement == \"%s\") " +
            "|> filter(fn: (r) => r.device_id == \"%s\") " +
            "|> filter(fn: (r) => r._field == \"%s\") " +
            "|> %s()",
            bucket,
            from.format(FORMATTER),
            to.format(FORMATTER),
            MEASUREMENT_TELEMETRY,
            deviceId,
            metricName,
            fluxFunction
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, org);

        if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
            return Optional.empty();
        }

        Object value = tables.get(0).getRecords().get(0).getValue();
        return value instanceof Number ? Optional.of(((Number) value).doubleValue()) : Optional.empty();
    }

    @Override
    public List<MetricPoint> aggregateMetricByWindow(
        String deviceId,
        String metricName,
        LocalDateTime from,
        LocalDateTime to,
        long window,
        AggregateType aggregate
    ) {
        String fluxFunction = mapAggregateType(aggregate);
        String windowDuration = String.format("%ds", window);

        String flux = String.format(
            "from(bucket: \"%s\") " +
            "|> range(start: %s, stop: %s) " +
            "|> filter(fn: (r) => r._measurement == \"%s\") " +
            "|> filter(fn: (r) => r.device_id == \"%s\") " +
            "|> filter(fn: (r) => r._field == \"%s\") " +
            "|> aggregateWindow(every: %s, fn: %s, createEmpty: false)",
            bucket,
            from.format(FORMATTER),
            to.format(FORMATTER),
            MEASUREMENT_TELEMETRY,
            deviceId,
            metricName,
            windowDuration,
            fluxFunction
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, org);

        return tables.stream()
            .flatMap(table -> table.getRecords().stream())
            .map(record -> new MetricPoint(
                LocalDateTime.ofInstant(
                    Instant.parse(record.getTime().toString()),
                    ZoneId.systemDefault()
                ),
                record.getValue() instanceof Number ? ((Number) record.getValue()).doubleValue() : null
            ))
            .collect(Collectors.toList());
    }

    @Override
    public double calculateReportFrequency(String deviceId, LocalDateTime from, LocalDateTime to) {
        String flux = String.format(
            "from(bucket: \"%s\") " +
            "|> range(start: %s, stop: %s) " +
            "|> filter(fn: (r) => r._measurement == \"%s\") " +
            "|> filter(fn: (r) => r.device_id == \"%s\") " +
            "|> count()",
            bucket,
            from.format(FORMATTER),
            to.format(FORMATTER),
            MEASUREMENT_TELEMETRY,
            deviceId
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, org);

        if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
            return 0;
        }

        Object count = tables.get(0).getRecords().get(0).getValue();
        long totalPoints = count instanceof Number ? ((Number) count).longValue() : 0;

        long durationSeconds = java.time.Duration.between(from, to).getSeconds();
        return durationSeconds > 0 ? (double) totalPoints / durationSeconds : 0;
    }

    @Override
    public double calculateDataIntegrity(String deviceId, LocalDateTime from, LocalDateTime to) {
        // 根据设备类型计算预期数据点数
        long expectedPoints = calculateExpectedPoints(deviceId, from, to);

        if (expectedPoints == 0) {
            return 0;
        }

        String flux = String.format(
            "from(bucket: \"%s\") " +
            "|> range(start: %s, stop: %s) " +
            "|> filter(fn: (r) => r._measurement == \"%s\") " +
            "|> filter(fn: (r) => r.device_id == \"%s\") " +
            "|> count()",
            bucket,
            from.format(FORMATTER),
            to.format(FORMATTER),
            MEASUREMENT_TELEMETRY,
            deviceId
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, org);

        if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
            return 0;
        }

        Object count = tables.get(0).getRecords().get(0).getValue();
        long actualPoints = count instanceof Number ? ((Number) count).longValue() : 0;

        return (double) actualPoints / expectedPoints;
    }

    @Override
    public DataQualityStats getDataQualityStats(String deviceId, LocalDateTime from, LocalDateTime to) {
        String flux = String.format(
            "from(bucket: \"%s\") " +
            "|> range(start: %s, stop: %s) " +
            "|> filter(fn: (r) => r._measurement == \"%s\") " +
            "|> filter(fn: (r) => r.device_id == \"%s\") " +
            "|> filter(fn: (r) => r._field == \"data_quality\") " +
            "|> group(columns: [\"_value\") " +
            "|> count()",
            bucket,
            from.format(FORMATTER),
            to.format(FORMATTER),
            MEASUREMENT_TELEMETRY,
            deviceId
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, org);

        long totalCount = 0;
        long goodCount = 0;
        long fairCount = 0;
        long poorCount = 0;

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object qualityValue = record.getValue();
                Object countValue = record.getRecordValue().get("_value");

                if (qualityValue instanceof Number && countValue instanceof Number) {
                    int quality = ((Number) qualityValue).intValue();
                    long count = ((Number) countValue).longValue();

                    totalCount += count;
                    if (quality == 1) goodCount += count;
                    else if (quality == 2) fairCount += count;
                    else if (quality == 3) poorCount += count;
                }
            }
        }

        return new DataQualityStats(totalCount, goodCount, fairCount, poorCount);
    }

    @Override
    public long deleteBefore(String deviceId, LocalDateTime before) {
        // InfluxDB 2.x 删除数据需要使用 deleteApi
        String fluxPredicate = String.format(
            "_measurement=\"%s\" AND device_id=\"%s\" AND _time < %s",
            MEASUREMENT_TELEMETRY,
            deviceId,
            before.format(FORMATTER)
        );

        try {
            influxDBClient.getDeleteApi().delete(
                before.minusYears(100),
                before,
                fluxPredicate,
                bucket,
                org
            );
            log.info("Deleted telemetry data for device {} before {}", deviceId, before);
            return 1; // InfluxDB 不返回删除数量
        } catch (Exception e) {
            log.error("Failed to delete telemetry data", e);
            return 0;
        }
    }

    // ==================== 私有方法 ====================

    private String buildLatestQuery(String deviceId) {
        return String.format(
            "from(bucket: \"%s\") " +
            "|> range(start: -1h) " +
            "|> filter(fn: (r) => r._measurement == \"%s\") " +
            "|> filter(fn: (r) => r.device_id == \"%s\") " +
            "|> last()",
            bucket, MEASUREMENT_TELEMETRY, deviceId
        );
    }

    private String buildTimeRangeQuery(String deviceId, LocalDateTime from, LocalDateTime to) {
        return String.format(
            "from(bucket: \"%s\") " +
            "|> range(start: %s, stop: %s) " +
            "|> filter(fn: (r) => r._measurement == \"%s\") " +
            "|> filter(fn: (r) => r.device_id == \"%s\")",
            bucket,
            from.format(FORMATTER),
            to.format(FORMATTER),
            MEASUREMENT_TELEMETRY,
            deviceId
        );
    }

    private String mapAggregateType(AggregateType aggregate) {
        switch (aggregate) {
            case MEAN: return "mean";
            case MAX: return "max";
            case MIN: return "min";
            case SUM: return "sum";
            case COUNT: return "count";
            case FIRST: return "first";
            case LAST: return "last";
            default: return "mean";
        }
    }

    private DeviceTelemetry mapToTelemetry(FluxRecord record) {
        // TODO: 实现 FluxRecord 到 DeviceTelemetry 的映射
        return null;
    }

    private long calculateExpectedPoints(String deviceId, LocalDateTime from, LocalDateTime to) {
        // 根据设备上报频率计算预期数据点数
        // TODO: 从设备配置中获取上报频率
        long durationMinutes = java.time.Duration.between(from, to).toMinutes();
        return durationMinutes; // 假设每分钟上报一次
    }
}
