package com.huakuangtong.iot.ingestion.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 设备型号信息
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Data
public class DeviceModelRequest {

    @NotBlank(message = "制造商不能为空")
    private String manufacturer;

    @NotBlank(message = "型号不能为空")
    private String model;

    private String firmwareVersion;
}
