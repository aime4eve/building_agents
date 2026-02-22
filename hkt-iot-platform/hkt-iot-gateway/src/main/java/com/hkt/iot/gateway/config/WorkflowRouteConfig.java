package com.hkt.iot.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工作流引擎路由配置
 * 配置工作流引擎相关的API路由
 *
 * @author HKT IoT Team
 */
@Configuration
public class WorkflowRouteConfig {

    /**
     * 工作流引擎路由配置
     */
    @Bean
    public RouteLocator workflowRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // 启动流程
                .route("workflow-process-start", r -> r
                        .path("/api/v1/workflow/process/start")
                        .and()
                        .method("POST")
                        .uri("lb://hkt-iot-workflow-service"))

                // 查询流程状态
                .route("workflow-process-status", r -> r
                        .path("/api/v1/workflow/process/**/status")
                        .and()
                        .method("GET")
                        .uri("lb://hkt-iot-workflow-service"))

                // 完成任务
                .route("workflow-task-complete", r -> r
                        .path("/api/v1/workflow/task/**/complete")
                        .and()
                        .method("POST")
                        .uri("lb://hkt-iot-workflow-service"))

                // 查询待办任务
                .route("workflow-task-pending", r -> r
                        .path("/api/v1/workflow/task/pending")
                        .and()
                        .method("GET")
                        .uri("lb://hkt-iot-workflow-service"))

                // 规则引擎评估
                .route("workflow-rule-evaluate", r -> r
                        .path("/api/v1/workflow/rule/evaluate")
                        .and()
                        .method("POST")
                        .uri("lb://hkt-iot-workflow-service"))

                .build();
    }
}
