package com.hkt.iot.device.interfaces.rest.dto;

import com.hkt.iot.device.domain.model.Device;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 设备更新DTO
 *
 * @author HKT IoT Team
 */
@Data
public class DeviceUpdateDTO {

    private String deviceName;
    private Long spaceId;
    private String locationDesc;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private BigDecimal altitude;
    private String firmwareVersion;
    private String hardwareVersion;
    private String softwareVersion;
    private Device.DeviceStatus deviceStatus;
}
