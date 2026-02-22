package com.hkt.iot.smartapps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智能应用服务启动类
 *
 * 包含：
 * - 防霉管控BC
 * - 智慧畜牧BC
 *
 * @author HKT IoT Team
 */
@SpringBootApplication(scanBasePackages = {
        "com.hkt.iot.smartapps",
        "com.hkt.iot.common",
        "com.hkt.iot.domain"
})
@EnableFeignClients(basePackages = {
        "com.hkt.iot.smartapps.interfaces.feign",
        "com.hkt.iot.common.feign"
})
@EnableScheduling
public class SmartAppsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartAppsServiceApplication.class, args);
    }
}
