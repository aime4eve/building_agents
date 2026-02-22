package com.huakuantong.iot.platform.infrastructure.timeseries;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.HealthCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

/**
 * InfluxDB 2.x 配置类
 *
 * <p>配置项：</p>
 * <ul>
 *   <li>timeseries.influx.url: InfluxDB服务器地址</li>
 *   <li>timeseries.influx.token: 访问令牌</li>
 *   <li>timeseries.influx.org: 组织名称</li>
 *   <li>timeseries.influx.bucket: 存储桶名称</li>
 * </ul>
 *
 * @author DDD Team
 * @version 1.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "timeseries.influx", name = "enabled", havingValue = "true", matchIfMissing = false)
public class InfluxDBConfig {

    @Value("${timeseries.influx.url:http://localhost:8086}")
    private String url;

    @Value("${timeseries.influx.token:}")
    private String token;

    @Value("${timeseries.influx.org:huakuantong}")
    private String org;

    @Value("${timeseries.influx.bucket:huakuantong_telemetry}")
    private String bucket;

    private InfluxDBClient influxDBClient;

    /**
     * 创建 InfluxDB 客户端
     */
    @Bean(destroyMethod = "close")
    public InfluxDBClient influxDBClient() {
        log.info("Initializing InfluxDB client: url={}, org={}, bucket={}", url, org, bucket);

        influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray());

        // 健康检查
        HealthCheck health = influxDBClient.health();
        if (health.getStatus().equals(HealthCheck.Status.PASS)) {
            log.info("InfluxDB connection successful");
        } else {
            log.warn("InfluxDB health check failed: {}", health.getMessage());
        }

        return influxDBClient;
    }

    /**
     * 获取组织名称
     */
    @Bean
    public String influxOrg() {
        return org;
    }

    /**
     * 获取存储桶名称
     */
    @Bean
    public String influxBucket() {
        return bucket;
    }

    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    public void close() {
        if (influxDBClient != null) {
            influxDBClient.close();
            log.info("InfluxDB client closed");
        }
    }
}
