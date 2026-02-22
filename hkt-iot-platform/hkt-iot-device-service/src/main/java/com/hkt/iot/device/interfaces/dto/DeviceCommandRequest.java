package com.hkt.iot.device.interfaces.rest.dto;

import com.hkt.iot.device.domain.model.DeviceCommand;
import lombok.Data;

import java.util.Map;

/**
 * 设备命令请求DTO
 *
 * @author HKT IoT Team
 */
@Data
public class DeviceCommandRequest {

    private Long deviceId;
    private String commandCode;
    private String commandName;
    private DeviceCommand.CommandType commandType;
    private Map<String, Object> inputParams;
    private Integer priority;
}
