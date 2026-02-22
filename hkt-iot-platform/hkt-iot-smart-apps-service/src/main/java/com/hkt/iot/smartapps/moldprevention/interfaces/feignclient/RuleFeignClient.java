package com.hkt.iot.smartapps.moldprevention.interfaces.feignclient;

import com.hkt.iot.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 规则引擎服务 Feign Client
 */
@FeignClient(name = "rule-service", contextId = "moldRuleClient")
public interface RuleFeignClient {

    /**
     * 获取防霉规则列表
     */
    @GetMapping("/api/v1/rules/mold-prevention")
    Result<List<Map<String, Object>>> getMoldPreventionRules(@RequestParam("tenantId") String tenantId);

    /**
     * 执行规则计算
     */
    @PostMapping("/api/v1/rules/execute")
    Result<Map<String, Object>> executeRule(@RequestBody Map<String, Object> request);

    /**
     * 批量执行规则
     */
    @PostMapping("/api/v1/rules/batch-execute")
    Result<List<Map<String, Object>>> batchExecuteRule(@RequestBody List<Map<String, Object>> requests);
}
