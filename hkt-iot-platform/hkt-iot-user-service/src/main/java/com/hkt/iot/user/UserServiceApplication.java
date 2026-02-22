package com.hkt.iot.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 用户与租户服务启动类
 *
 * @author HKT IoT Team
 */
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableTransactionManagement
@MapperScan("com.hkt.iot.user.infrastructure.persistence.mapper")
@SpringBootApplication(scanBasePackages = {
        "com.hkt.iot.user",
        "com.hkt.iot.common"
})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
