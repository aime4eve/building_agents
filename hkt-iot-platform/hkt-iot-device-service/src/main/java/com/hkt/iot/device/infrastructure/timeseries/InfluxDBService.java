package com.hkt.iot.device.infrastructure.timeseries;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * InfluxDB时序数据库服务
 * 负责遥测数据的时序存储与查询（读侧时序模型）
 *
 * @author HKT IoT Team
 */
@Service
@Slf4j
public class InfluxDBService {

    @Value("${influxdb.url:http://localhost:8086}")
    private String influxdbUrl;

    @Value("${influxdb.token:}")
    private String influxdbToken;

    @Value("${influxdb.org:hkt-iot}")
    private String influxdbOrg;

    @Value("${influxdb.bucket:telemetry}")
    private String influxdbBucket;

    private InfluxDBClient influxDBClient;
    private WriteApi writeApi;

    @PostConstruct
    public void init() {
        try {
            influxDBClient = InfluxDBClientFactory.create(
                    influxdbUrl,
                    influxdbToken.toCharArray(),
                    influxdbOrg,
                    influxdbBucket
            );
            writeApi = influxDBClient.makeWriteApi();
            log.info("InfluxDB连接成功: url={}, bucket={}", influxdbUrl, influxdbBucket);
        } catch (Exception e) {
            log.error("InfluxDB连接失败: error={}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (writeApi != null) {
            writeApi.close();
        }
        if (influxDBClient != null) {
            influxDBClient.close();
        }
    }

    /**
     * 写入遥测数据
     *
     * @param deviceSn 设备序列号
     * @param data     遥测数据
     * @param dataTime 数据时间
     */
    public void writeTelemetry(String deviceSn, Map<String, Object> data, LocalDateTime dataTime) {
        try {
            Point point = Point.measurement("device_telemetry")
                    .addTag("device_sn", deviceSn)
                    .addTag("tenant_id", String.valueOf(data.get("tenantId")))
                    .time(dataTime.atZone(ZoneId.systemDefault()).toInstant(), WritePrecision.NS);

            // 添加所有数据字段
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if ("tenantId".equals(entry.getKey()) || "deviceId".equals(entry.getKey())) {
                    continue;
                }
                Object value = entry.getValue();
                if (value instanceof Number) {
                    point.addField(entry.getKey(), (Number) value);
                } else if (value instanceof Boolean) {
                    point.addField(entry.getKey(), (Boolean) value);
                } else {
                    point.addField(entry.getKey(), value.toString());
                }
            }

            writeApi.writePoint(point);
            log.debug("遥测数据写入InfluxDB成功: deviceSn={}", deviceSn);

        } catch (Exception e) {
            log.error("遥测数据写入InfluxDB失败: deviceSn={}, error={}",
                    deviceSn, e.getMessage(), e);
            throw new RuntimeException("时序数据写入失败", e);
        }
    }

    /**
     * 批量写入遥测数据
     */
    public void writeTelemetryBatch(List<TelemetryPoint> points) {
        try {
            List<Point> influxPoints = points.stream()
                    .map(p -> {
                        Point point = Point.measurement("device_telemetry")
                                .addTag("device_sn", p.getDeviceSn())
                                .addTag("data_key", p.getDataKey())
                                .time(p.getTime(), WritePrecision.NS);

                        if (p.getValue() instanceof Number) {
                            point.addField("value", (Number) p.getValue());
                        } else if (p.getValue() instanceof Boolean) {
                            point.addField("value", (Boolean) p.getValue());
                        } else {
                            point.addField("value", p.getValue().toString());
                        }

                        return point;
                    })
                    .collect(Collectors.toList());

            writeApi.writePoints(influxPoints);
            log.debug("批量遥测数据写入InfluxDB成功: count={}", points.size());

        } catch (Exception e) {
            log.error("批量遥测数据写入InfluxDB失败: error={}", e.getMessage(), e);
            throw new RuntimeException("批量时序数据写入失败", e);
        }
    }

    /**
     * 查询遥测数据
     *
     * @param deviceSn  设备序列号
     * @param dataKey   数据键
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     限制数量
     * @return 查询结果
     */
    public List<Map<String, Object>> queryTelemetry(
            String deviceSn,
            String dataKey,
            LocalDateTime startTime,
            LocalDateTime endTime,
            long limit) {

        try {
            String flux = String.format(
                    "from(bucket: \"%s\")\n" +
                            "  |> range(start: %s, stop: %s)\n" +
                            "  |> filter(fn: (r) => r._measurement == \"device_telemetry\")\n" +
                            "  |> filter(fn: (r) => r.device_sn == \"%s\")\n" +
                            "  |> filter(fn: (r) => r._field == \"%s\")\n" +
                            "  |> limit(n: %d)",
                    influxdbBucket,
                    startTime.atZone(ZoneId.systemDefault()).toInstant().toString(),
                    endTime.atZone(ZoneId.systemDefault()).toInstant().toString(),
                    deviceSn,
                    dataKey,
                    limit
            );

            List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, influxdbOrg);

            return tables.stream()
                    .flatMap(table -> table.getRecords().stream())
                    .map(this::convertRecordToMap)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("查询InfluxDB失败: deviceSn={}, dataKey={}, error={}",
                    deviceSn, dataKey, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 聚合查询
     *
     * @param deviceSn        设备序列号
     * @param dataKey         数据键
     * @param startTime       开始时间
     * @param endTime         结束时间
     * @param aggregationType 聚合类型: mean, max, min, sum, count
     * @return 聚合结果
     */
    public Map<String, Object> aggregateTelemetry(
            String deviceSn,
            String dataKey,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String aggregationType) {

        try {
            String flux = String.format(
                    "from(bucket: \"%s\")\n" +
                            "  |> range(start: %s, stop: %s)\n" +
                            "  |> filter(fn: (r) => r._measurement == \"device_telemetry\")\n" +
                            "  |> filter(fn: (r) => r.device_sn == \"%s\")\n" +
                            "  |> filter(fn: (r) => r._field == \"%s\")\n" +
                            "  |> %s(column: \"_value\")",
                    influxdbBucket,
                    startTime.atZone(ZoneId.systemDefault()).toInstant().toString(),
                    endTime.atZone(ZoneId.systemDefault()).toInstant().toString(),
                    deviceSn,
                    dataKey,
                    aggregationType
            );

            List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, influxdbOrg);

            if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
                return Collections.emptyMap();
            }

            FluxRecord record = tables.get(0).getRecords().get(0);
            Map<String, Object> result = new HashMap<>();
            result.put("aggregation", aggregationType);
            result.put("value", record.getValueByKey("_value"));
            result.put("time", record.getTime());

            return result;

        } catch (Exception e) {
            log.error("聚合查询InfluxDB失败: deviceSn={}, dataKey={}, error={}",
                    deviceSn, dataKey, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 查询最新数据
     */
    public Map<String, Object> queryLatest(String deviceSn) {
        try {
            String flux = String.format(
                    "from(bucket: \"%s\")\n" +
                            "  |> range(start: -1h)\n" +
                            "  |> filter(fn: (r) => r._measurement == \"device_telemetry\")\n" +
                            "  |> filter(fn: (r) => r.device_sn == \"%s\")\n" +
                            "  |> last(column: \"_time\")",
                    influxdbBucket,
                    deviceSn
            );

            List<FluxTable> tables = influxDBClient.getQueryApi().query(flux, influxdbOrg);

            return tables.stream()
                    .flatMap(table -> table.getRecords().stream())
                    .collect(Collectors.groupingBy(
                            r -> r.getValueByKey("_field").toString(),
                            Collectors.mapping(
                                    FluxRecord::getValue,
                                    Collectors.toList()
                            )
                    ));

        } catch (Exception e) {
            log.error("查询最新数据失败: deviceSn={}, error={}",
                    deviceSn, e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 转换Flux记录为Map
     */
    private Map<String, Object> convertRecordToMap(FluxRecord record) {
        Map<String, Object> map = new HashMap<>();
        map.put("time", record.getTime());
        map.put("value", record.getValue());
        map.put("field", record.getField());
        map.put("measurement", record.getMeasurement());

        // 添加所有标签
        for (Map.Entry<String, Object> entry : record.getValues().entrySet()) {
            if (entry.getKey().startsWith("_")) {
                continue;
            }
            map.put(entry.getKey(), entry.getValue());
        }

        return map;
    }

    /**
     * 遥测数据点
     */
    public static class TelemetryPoint {
        private final String deviceSn;
        private final String dataKey;
        private final Object value;
        private final Instant time;

        public TelemetryPoint(String deviceSn, String dataKey, Object value, Instant time) {
            this.deviceSn = deviceSn;
            this.dataKey = dataKey;
            this.value = value;
            this.time = time;
        }

        public String getDeviceSn() { return deviceSn; }
        public String getDataKey() { return dataKey; }
        public Object getValue() { return value; }
        public Instant getTime() { return time; }
    }
}
