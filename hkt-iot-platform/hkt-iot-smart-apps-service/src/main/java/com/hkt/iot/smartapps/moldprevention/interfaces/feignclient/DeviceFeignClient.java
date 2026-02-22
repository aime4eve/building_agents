package com.hkt.iot.smartapps.moldprevention.interfaces.feignclient;

import com.hkt.iot.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 设备管理服务 Feign Client
 */
@FeignClient(name = "device-service", contextId = "moldDeviceClient")
public interface DeviceFeignClient {

    /**
     * 获取设备最新遥测数据
     */
    @GetMapping("/api/v1/devices/{deviceId}/telemetry/latest")
    Result<Map<String, Object>> getLatestTelemetry(@PathVariable("deviceId") String deviceId);

    /**
     * 获取多个设备的最新遥测数据
     */
    @PostMapping("/api/v1/devices/telemetry/latest/batch")
    Result<Map<String, Map<String, Object>>> getBatchLatestTelemetry(@RequestBody Map<String, String> deviceIds);

    /**
     * 发送设备控制命令
     */
    @PostMapping("/api/v1/devices/{deviceId}/commands")
    Result<Void> sendCommand(
        @PathVariable("deviceId") String deviceId,
        @RequestBody Map<String, Object> command
    );

    /**
     * 根据空间 ID 查询设备列表
     */
    @GetMapping("/api/v1/devices/space/{spaceId}")
    Result<Map<String, Object>> getDevicesBySpace(@PathVariable("spaceId") Long spaceId);
}
