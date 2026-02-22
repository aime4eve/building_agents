package com.hkt.iot.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 设备接入层路由配置
 * 配置设备注册、状态更新的API路由
 *
 * @author HKT IoT Team
 */
@Configuration
public class DeviceLayerRouteConfig {

    /**
     * 设备接入层路由配置
     * 将设备注册和状态相关的请求路由到设备管理服务
     */
    @Bean
    public RouteLocator deviceLayerRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // 设备注册路由
                .route("device-registry-register", r -> r
                        .path("/api/v1/device-registry/register")
                        .and()
                        .method("POST")
                        .uri("lb://hkt-iot-device-service"))

                // 设备证书续期路由
                .route("device-registry-renew-cert", r -> r
                        .path("/api/v1/device-registry/renew-cert")
                        .and()
                        .method("POST")
                        .uri("lb://hkt-iot-device-service"))

                // 设备查询路由
                .route("device-registry-get", r -> r
                        .path("/api/v1/device-registry/**")
                        .and()
                        .method("GET")
                        .uri("lb://hkt-iot-device-service"))

                // Token刷新路由
                .route("device-registry-refresh-token", r -> r
                        .path("/api/v1/device-registry/*/refresh-token")
                        .and()
                        .method("POST")
                        .uri("lb://hkt-iot-device-service"))

                // 设备上线通知路由
                .route("device-status-online", r -> r
                        .path("/api/v1/device-status/online")
                        .and()
                        .method("POST")
                        .uri("lb://hkt-iot-device-service"))

                // 设备离线通知路由
                .route("device-status-offline", r -> r
                        .path("/api/v1/device-status/offline")
                        .and()
                        .method("POST")
                        .uri("lb://hkt-iot-device-service"))

                // 设备状态查询路由
                .route("device-status-query", r -> r
                        .path("/api/v1/device-status/**")
                        .and()
                        .method("GET")
                        .uri("lb://hkt-iot-device-service"))

                .build();
    }
}
