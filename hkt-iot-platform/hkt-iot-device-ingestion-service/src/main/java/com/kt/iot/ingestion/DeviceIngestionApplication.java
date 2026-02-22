package com.hkt.iot.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 设备接入服务启动类
 *
 * 负责处理设备上报的MQTT消息，并通过Kafka桥接到后端服务
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@SpringBootApplication(scanBasePackages = {"com.hkt.iot.ingestion", "com.hkt.iot.common"})
@EnableDiscoveryClient
@EnableKafka
@EnableScheduling
@IntegrationComponentScan
public class DeviceIngestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceIngestionApplication.class, args);
    }
}
