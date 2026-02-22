package com.hkt.iot.device.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 遥测数据实体
 * 存储设备上报的实时遥测数据（写侧快照）
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "telemetry_data", indexes = {
    @Index(name = "idx_device_time", columnList = "device_id,data_time"),
    @Index(name = "idx_tenant_time", columnList = "tenant_id,data_time")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TelemetryData extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "device_sn", nullable = false, length = 100)
    private String deviceSn;

    @Column(name = "data_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DataType dataType;

    @Column(name = "data_key", nullable = false, length = 100)
    private String dataKey;

    /**
     * 字符串类型值
     */
    @Column(name = "string_value", length = 500)
    private String stringValue;

    /**
     * 整数类型值
     */
    @Column(name = "long_value")
    private Long longValue;

    /**
     * 小数类型值
     */
    @Column(name = "double_value", precision = 20, scale = 6)
    private Double doubleValue;

    /**
     * 布尔类型值
     */
    @Column(name = "boolean_value")
    private Boolean booleanValue;

    /**
     * JSON类型值
     */
    @Column(name = "json_value", columnDefinition = "TEXT")
    private String jsonValue;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "data_time", nullable = false)
    private LocalDateTime dataTime;

    @Column(name = "receive_time", nullable = false)
    private LocalDateTime receiveTime;

    @Column(name = "quality_code", length = 20)
    @Enumerated(EnumType.STRING)
    private QualityCode qualityCode;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "altitude", precision = 8, scale = 2)
    private BigDecimal altitude;

    @Column(name = "event_id", length = 100)
    private String eventId;

    @Column(name = "batch_id", length = 100)
    private String batchId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 数据类型
     */
    public enum DataType {
        PROPERTY,    // 属性数据
        EVENT,       // 事件数据
        METRIC       // 指标数据
    }

    /**
     * 数据质量码
     */
    public enum QualityCode {
        GOOD,        // 良好
        UNCERTAIN,   // 不确定
        BAD,         // 坏值
        INVALID      // 无效
    }

    /**
     * 工厂方法：创建遥测数据
     */
    public static TelemetryData create(
            Long tenantId,
            Long deviceId,
            String deviceSn,
            DataType dataType,
            String dataKey,
            Object value,
            String unit,
            LocalDateTime dataTime,
            QualityCode qualityCode,
            String eventId,
            String batchId) {

        TelemetryData telemetry = new TelemetryData();
        telemetry.tenantId = tenantId;
        telemetry.deviceId = deviceId;
        telemetry.deviceSn = deviceSn;
        telemetry.dataType = dataType;
        telemetry.dataKey = dataKey;
        telemetry.unit = unit;
        telemetry.dataTime = dataTime;
        telemetry.receiveTime = LocalDateTime.now();
        telemetry.qualityCode = qualityCode != null ? qualityCode : QualityCode.GOOD;
        telemetry.eventId = eventId;
        telemetry.batchId = batchId;
        telemetry.deleted = false;
        telemetry.createdAt = LocalDateTime.now();

        // 根据值类型设置对应字段
        telemetry.setValue(value);

        return telemetry;
    }

    /**
     * 设置值
     */
    private void setValue(Object value) {
        if (value == null) {
            return;
        }

        if (value instanceof String) {
            this.stringValue = (String) value;
        } else if (value instanceof Long || value instanceof Integer) {
            this.longValue = ((Number) value).longValue();
        } else if (value instanceof Double || value instanceof Float || value instanceof BigDecimal) {
            this.doubleValue = ((Number) value).doubleValue();
        } else if (value instanceof Boolean) {
            this.booleanValue = (Boolean) value;
        } else {
            // 复杂对象转JSON
            this.jsonValue = convertToJson(value);
        }
    }

    /**
     * 获取值
     */
    public Object getValue() {
        if (this.jsonValue != null) {
            return parseJson(this.jsonValue);
        }
        if (this.booleanValue != null) {
            return this.booleanValue;
        }
        if (this.doubleValue != null) {
            return this.doubleValue;
        }
        if (this.longValue != null) {
            return this.longValue;
        }
        return this.stringValue;
    }

    /**
     * 转换为JSON字符串
     */
    private String convertToJson(Object value) {
        // 简化实现，实际应使用JSON库
        return value.toString();
    }

    /**
     * 解析JSON字符串
     */
    private Object parseJson(String json) {
        // 简化实现，实际应使用JSON库
        return json;
    }

    /**
     * 检查数据质量是否良好
     */
    public boolean isGoodQuality() {
        return this.qualityCode == QualityCode.GOOD;
    }

    /**
     * 检查数据是否有效
     */
    public boolean isValid() {
        return this.qualityCode != QualityCode.INVALID;
    }

    /**
     * 软删除
     */
    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
