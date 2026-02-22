package com.huakuantong.iot.platform.infrastructure.timeseries;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * TDengine 配置属性
 *
 * @author DDD Team
 * @version 1.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "timeseries.tdengine")
public class TDengineProperties {

    /**
     * TDengine JDBC URL
     * 格式: jdbc:TAOS://host:6030/dbname
     */
    private String url = "jdbc:TAOS://localhost:6030/telemetry";

    /**
     * 用户名
     */
    private String username = "root";

    /**
     * 密码
     */
    private String password = "taosdata";

    /**
     * 数据库名称
     */
    private String database = "telemetry";

    /**
     * 连接池配置
     */
    private PoolConfig pool = new PoolConfig();

    @Data
    public static class PoolConfig {
        /**
         * 最小空闲连接数
         */
        private int minIdle = 5;

        /**
         * 最大连接数
         */
        private int maxActive = 50;

        /**
         * 连接超时时间（毫秒）
         */
        private long connectionTimeout = 30000;

        /**
         * 最大等待时间（毫秒）
         */
        private long maxWaitTime = 60000;
    }
}
