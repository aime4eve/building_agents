package com.hkt.iot.smartapps.moldprevention.interfaces.feignclient;

import com.hkt.iot.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 空间管理服务 Feign Client
 */
@FeignClient(name = "space-service", contextId = "moldSpaceClient")
public interface SpaceFeignClient {

    /**
     * 获取空间详情
     */
    @GetMapping("/api/v1/spaces/{spaceId}")
    Result<Map<String, Object>> getSpace(@PathVariable("spaceId") Long spaceId);

    /**
     * 获取空间层级树
     */
    @GetMapping("/api/v1/spaces/tree/{rootSpaceId}")
    Result<Map<String, Object>> getSpaceTree(@PathVariable("rootSpaceId") Long rootSpaceId);
}
