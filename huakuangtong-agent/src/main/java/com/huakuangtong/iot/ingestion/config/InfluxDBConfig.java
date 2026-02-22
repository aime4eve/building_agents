package com.huakuangtong.iot.ingestion.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * InfluxDB配置类
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Configuration
@ConfigurationProperties(prefix = "influx")
@Data
public class InfluxDBConfig {

    private String url;
    private String token;
    private String org;
    private BucketConfig bucket = new BucketConfig();
    private WriteConfig write = new WriteConfig();

    @Data
    public static class BucketConfig {
        private String raw = "hkt_iot_raw";
        private String event = "hkt_iot_event";
        private String status = "hkt_iot_status";
    }

    @Data
    public static class WriteConfig {
        private int batchSize = 1000;
        private int flushInterval = 1000;
        private int bufferLimit = 10000;
    }

    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket.raw);
    }
}
