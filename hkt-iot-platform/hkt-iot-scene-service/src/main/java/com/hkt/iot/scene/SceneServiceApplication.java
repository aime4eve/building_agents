package com.hkt.iot.scene;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 场景联动服务启动类
 *
 * @author HKT IoT Team
 */
@SpringBootApplication(scanBasePackages = {"com.hkt.iot.scene", "com.hkt.iot.common", "com.hkt.iot.domain"})
@EnableFeignClients(basePackages = {"com.hkt.iot.scene.interfaces.feign", "com.hkt.iot.common.feign"})
@EnableScheduling
public class SceneServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SceneServiceApplication.class, args);
    }
}
