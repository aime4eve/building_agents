package com.huakuantong.iot.platform.infrastructure.timeseries;

import com.taosdata.jdbc.TSDBDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * TDengine 配置类
 *
 * <p>配置项：</p>
 * <ul>
 *   <li>timeseries.tdengine.url: TDengine JDBC URL</li>
 *   <li>timeseries.tdengine.username: 用户名</li>
 *   <li>timeseries.tdengine.password: 密码</li>
 *   <li>timeseries.tdengine.database: 数据库名称</li>
 * </ul>
 *
 * <p>JDBC URL 格式：</p>
 * <pre>
 * jdbc:TAOS://host:6030/dbname
 * </pre>
 *
 * @author DDD Team
 * @version 1.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "timeseries.tdengine", name = "enabled", havingValue = "true", matchIfMissing = false)
public class TDengineConfig {

    /**
     * 创建 TDengine 数据源
     */
    @Bean
    @Primary
    public DataSource tdengineDataSource(
        TDengineProperties properties
    ) {
        log.info("Initializing TDengine datasource: url={}, database={}",
            properties.getUrl(), properties.getDatabase());

        DataSourceBuilder<?> builder = DataSourceBuilder.create();
        builder.driverClassName(TSDBDriver.class.getName());
        builder.url(properties.getUrl());
        builder.username(properties.getUsername());
        builder.password(properties.getPassword());

        DataSource dataSource = builder.build();

        // 初始化数据库（如果不存在）
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String sql = String.format("CREATE DATABASE IF NOT EXISTS %s KEEP 90 UPDATE 1",
                    properties.getDatabase());
                stmt.execute(sql);
                log.info("TDengine database {} is ready", properties.getDatabase());
            }
        } catch (SQLException e) {
            log.error("Failed to initialize TDengine database", e);
        }

        return dataSource;
    }
}
