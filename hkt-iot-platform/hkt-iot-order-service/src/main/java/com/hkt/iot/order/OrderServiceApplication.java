package com.hkt.iot.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 订单与交易中心服务启动类
 *
 * @author HKT IoT Team
 */
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableTransactionManagement
@MapperScan("com.hkt.iot.order.infrastructure.persistence.mapper")
@SpringBootApplication(scanBasePackages = {
        "com.hkt.iot.order",
        "com.hkt.iot.common"
})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
