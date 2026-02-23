package com.hkt.iot.workflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 工作流引擎服务启动类
 *
 * @author HKT IoT Team
 */
@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@ComponentScan(basePackages = {"com.hkt.iot.workflow"})
@EntityScan(basePackages = {"com.hkt.iot.workflow.infrastructure.persistence.po"})
@MapperScan(basePackages = {"com.hkt.iot.workflow.infrastructure.persistence.mapper"})
public class WorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowApplication.class, args);
    }
}
