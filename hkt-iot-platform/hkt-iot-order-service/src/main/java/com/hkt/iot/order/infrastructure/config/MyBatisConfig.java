package com.hkt.iot.order.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置
 *
 * @author HKT IoT Team
 */
@Configuration
@MapperScan("com.hkt.iot.order.infrastructure.persistence.mapper")
public class MyBatisConfig {
}
