package com.hkt.iot.device.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 设备物模型实体
 * 基于DDL: device_thing_model表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "device_thing_model")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceThingModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "device_model", nullable = false, length = 100)
    private String deviceModel;

    @Column(name = "model_name", nullable = false, length = 200)
    private String modelName;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "properties_def", columnDefinition = "JSON")
    @Transient
    private List<PropertyDef> propertiesDef;

    @Column(name = "services_def", columnDefinition = "JSON")
    @Transient
    private List<ServiceDef> servicesDef;

    @Column(name = "events_def", columnDefinition = "JSON")
    @Transient
    private List<EventDef> eventsDef;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    /**
     * 属性定义
     */
    public record PropertyDef(
            String name,
            String identifier,
            String dataType,
            String unit,
            Double min,
            Double max,
            String spec
    ) {}

    /**
     * 服务定义
     */
    public record ServiceDef(
            String name,
            String identifier,
            List<InputArg> inputArgs,
            List<OutputArg> outputArgs,
            String callType
    ) {}

    /**
     * 事件定义
     */
    public record EventDef(
            String name,
            String identifier,
            String eventType,
            List<OutputArg> outputArgs,
            String desc
    ) {}

    /**
     * 输入参数
     */
    public record InputArg(
            String name,
            String dataType,
            String desc
    ) {}

    /**
     * 输出参数
     */
    public record OutputArg(
            String name,
            String dataType,
            String desc
    ) {}

    /**
     * 工厂方法：创建物模型
     */
    public static DeviceThingModel create(
            Long tenantId,
            String deviceModel,
            String modelName,
            String category,
            String manufacturer,
            List<PropertyDef> propertiesDef,
            List<ServiceDef> servicesDef,
            List<EventDef> eventsDef,
            Long createdBy) {
        DeviceThingModel model = new DeviceThingModel();
        model.tenantId = tenantId;
        model.deviceModel = deviceModel;
        model.modelName = modelName;
        model.category = category;
        model.manufacturer = manufacturer;
        model.propertiesDef = propertiesDef;
        model.servicesDef = servicesDef;
        model.eventsDef = eventsDef;
        model.status = 1;
        model.deleted = false;
        model.createdAt = LocalDateTime.now();
        model.updatedAt = LocalDateTime.now();
        model.createdBy = createdBy;
        model.updatedBy = createdBy;
        model.version = 0L;
        return model;
    }

    /**
     * 软删除
     */
    public void softDelete(Long deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.updatedAt = LocalDateTime.now();
    }
}
