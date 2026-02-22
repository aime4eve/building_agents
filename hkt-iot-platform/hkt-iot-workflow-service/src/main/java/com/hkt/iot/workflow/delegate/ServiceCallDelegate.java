package com.hkt.iot.workflow.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 通用服务调用Delegate
 * 通过HTTP调用外部服务API
 *
 * @author HKT IoT Team
 */
@Component
@Scope("prototype")
@Slf4j
public class ServiceCallDelegate implements JavaDelegate {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            // 获取配置参数
            String serviceUrl = (String) execution.getVariable("serviceUrl");
            String httpMethod = (String) execution.getVariable("httpMethod");
            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) execution.getVariable("headers");
            @SuppressWarnings("unchecked")
            Map<String, Object> requestBody = (Map<String, Object>) execution.getVariable("requestBody");

            if (serviceUrl == null || serviceUrl.isEmpty()) {
                throw new IllegalArgumentException("serviceUrl is required");
            }

            log.info("Calling service: url={}, method={}", serviceUrl, httpMethod);

            // 创建HTTP请求
            HttpMethod method = httpMethod != null 
                ? HttpMethod.valueOf(httpMethod.toUpperCase()) 
                : HttpMethod.POST;

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            
            if (headers != null) {
                headers.forEach(httpHeaders::add);
            }

            HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, httpHeaders);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                serviceUrl,
                method,
                requestEntity,
                String.class
            );

            // 存储响应结果
            execution.setVariable("serviceResponse", response.getBody());
            execution.setVariable("serviceStatusCode", response.getStatusCode().value());

            log.info("Service call completed: status={}", response.getStatusCode());

        } catch (Exception e) {
            log.error("Service call failed", e);
            execution.setVariable("serviceError", e.getMessage());
            throw new BpmnError("SERVICE_CALL_ERROR", 
                "Failed to call service: " + e.getMessage());
        }
    }
}
