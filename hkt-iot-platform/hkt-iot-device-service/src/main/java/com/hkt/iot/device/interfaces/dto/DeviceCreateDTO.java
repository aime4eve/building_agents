package com.hkt.iot.device.interfaces.rest.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 设备创建DTO
 *
 * @author HKT IoT Team
 */
@Data
public class DeviceCreateDTO {

    private Long tenantId;
    private String deviceSn;
    private String deviceName;
    private String deviceCode;
    private String deviceType;
    private String deviceModel;
    private String deviceCategory;
    private Long thingModelId;
    private Long spaceId;
    private Long parentDeviceId;
    private String locationDesc;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private BigDecimal altitude;
    private String firmwareVersion;
    private String hardwareVersion;
    private String softwareVersion;
}
