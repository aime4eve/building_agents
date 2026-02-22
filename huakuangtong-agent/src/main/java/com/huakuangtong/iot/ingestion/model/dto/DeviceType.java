package com.huakuangtong.iot.ingestion.model.dto;

/**
 * 设备类型枚举
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
public enum DeviceType {

    // 表计类
    WATER_METER("water_meter", "水表"),
    ELECTRIC_METER("electric_meter", "电表"),
    GAS_METER("gas_meter", "气表"),

    // 传感器类
    SMOKE_DETECTOR("smoke_detector", "烟雾探测器"),
    TEMPERATURE_SENSOR("temperature_sensor", "温度传感器"),
    HUMIDITY_SENSOR("humidity_sensor", "湿度传感器"),
    TRASH_FULL_DETECTOR("trash_full_detector", "垃圾满溢探测器"),
    LIGHT_SENSOR("light_sensor", "光照传感器"),
    GEOMAGNETIC_DETECTOR("geomagnetic_detector", "地磁探测器"),
    DOOR_CONTACT("door_contact", "门磁传感器"),

    // 控制器类
    SOLENOID_VALVE("solenoid_valve", "电磁阀"),
    DOOR_LOCK("door_lock", "门锁"),
    PARKING_LOCK("parking_lock", "车位锁"),
    AIR_CONDITIONER("air_conditioner", "空调"),
    LIGHT("light", "灯光"),

    // 畜牧设备类
    ANIMAL_TRACKER("animal_tracker", "动物追踪器"),
    RUMEN_CAPSULE("rumen_capsule", "瘤胃胶囊"),

    // 网络设备类
    GATEWAY("gateway", "网关");

    private final String topicValue;
    private final String description;

    DeviceType(String topicValue, String description) {
        this.topicValue = topicValue;
        this.description = description;
    }

    public String toTopicValue() {
        return topicValue;
    }

    public String getDescription() {
        return description;
    }

    public static DeviceType fromTopicValue(String topicValue) {
        for (DeviceType type : values()) {
            if (type.topicValue.equalsIgnoreCase(topicValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown device type: " + topicValue);
    }
}
