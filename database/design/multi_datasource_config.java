package com.huakuantong.iot.platform.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 多数据源配置类
 *
 * <p>支持：</p>
 * <ul>
 *   <li>MySQL - 业务数据（主数据源）</li>
 *   <li>InfluxDB - 时序数据（只读）</li>
 *   <li>TDengine - 时序数据（只读，备选）</li>
 * </ul>
 *
 * @author DDD Team
 * @version 1.0
 */
@Configuration
public class MultiDataSourceConfig {

    /**
     * MySQL主数据源配置
     */
    @Primary
    @Bean(name = "mysqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.mysql")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .build();
    }

    /**
     * MySQL JdbcTemplate
     */
    @Primary
    @Bean(name = "mysqlJdbcTemplate")
    public JdbcTemplate mysqlJdbcTemplate() {
        return new JdbcTemplate(mysqlDataSource());
    }

    /**
     * InfluxDB数据源配置
     */
    @Bean(name = "influxDBProperties")
    @ConfigurationProperties(prefix = "timeseries.influx")
    @ConditionalOnProperty(prefix = "timeseries.influx", name = "enabled", havingValue = "true")
    public InfluxDBProperties influxDBProperties() {
        return new InfluxDBProperties();
    }

    /**
     * TDengine数据源配置
     */
    @Bean(name = "tdengineDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.tdengine")
    @ConditionalOnProperty(prefix = "timeseries.tdengine", name = "enabled", havingValue = "true")
    public DataSource tdengineDataSource() {
        return DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .driverClassName("com.taosdata.jdbc.TSDBDriver")
            .build();
    }

    /**
     * TDengine JdbcTemplate
     */
    @Bean(name = "tdengineJdbcTemplate")
    @ConditionalOnProperty(prefix = "timeseries.tdengine", name = "enabled", havingValue = "true")
    public JdbcTemplate tdengineJdbcTemplate() {
        return new JdbcTemplate(tdengineDataSource());
    }

    /**
     * InfluxDB配置属性类
     */
    public static class InfluxDBProperties {
        private String url;
        private String token;
        private String org;
        private String bucket;

        // Getters and Setters
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getOrg() { return org; }
        public void setOrg(String org) { this.org = org; }

        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }
    }
}
