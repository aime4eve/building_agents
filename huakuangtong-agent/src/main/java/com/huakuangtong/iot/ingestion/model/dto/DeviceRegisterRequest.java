package com.huakuangtong.iot.ingestion.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 设备注册请求
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Data
public class DeviceRegisterRequest {

    @NotBlank(message = "设备序列号不能为空")
    private String deviceSn;

    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    @NotNull(message = "设备型号不能为空")
    private DeviceModelRequest deviceModel;

    @NotBlank(message = "租户ID不能为空")
    private String tenantId;

    private String spaceId;
}
