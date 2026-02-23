package com.hkt.iot.space.domain.model;

import com.hkt.iot.domain.model.AggregateRoot;
import com.hkt.iot.space.domain.event.SpaceBoundsUpdatedEvent;
import com.hkt.iot.space.domain.event.SpaceDeletedEvent;
import com.hkt.iot.space.domain.event.SpaceStatusChangedEvent;
import com.hkt.iot.space.domain.event.SpaceUpdatedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 空间聚合根
 * 基于DDL: space表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "space")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Space extends AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "space_code", nullable = false, length = 100)
    private String spaceCode;

    @Column(name = "space_name", nullable = false, length = 200)
    private String spaceName;

    @Column(name = "space_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private SpaceType spaceType;

    @Column(name = "space_level", nullable = false)
    private Integer spaceLevel;

    @Column(name = "parent_space_id")
    private Long parentSpaceId;

    @Column(name = "root_space_id")
    private Long rootSpaceId;

    @Column(name = "space_path", length = 500)
    private String spacePath;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "province", length = 50)
    private String province;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "district", length = 50)
    private String district;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "altitude", precision = 8, scale = 2)
    private BigDecimal altitude;

    @Column(name = "boundary", columnDefinition = "JSON")
    @Transient
    private List<List<BigDecimal>> boundary;

    @Transient
    private SpatialBounds spatialBounds;

    @Column(name = "area", precision = 10, scale = 2)
    private BigDecimal area;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(name = "room_number", length = 50)
    private String roomNumber;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "space_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SpaceStatus spaceStatus;

    @Column(name = "usage_status", length = 20)
    @Enumerated(EnumType.STRING)
    private UsageStatus usageStatus;

    @Column(name = "ext_properties", columnDefinition = "JSON")
    @Transient
    private Map<String, Object> extProperties;

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
     * 空间类型
     */
    public enum SpaceType {
        PARK, BUILDING, FLOOR, ROOM
    }

    /**
     * 空间状态
     */
    public enum SpaceStatus {
        ACTIVE, INACTIVE, MAINTENANCE
    }

    /**
     * 使用状态
     */
    public enum UsageStatus {
        OCCUPIED, VACANT, RESERVED
    }

    /**
     * 工厂方法：创建空间
     */
    public static Space create(
            Long tenantId,
            String spaceCode,
            String spaceName,
            SpaceType spaceType,
            Integer spaceLevel,
            Long createdBy) {
        Space space = new Space();
        space.tenantId = tenantId;
        space.spaceCode = spaceCode;
        space.spaceName = spaceName;
        space.spaceType = spaceType;
        space.spaceLevel = spaceLevel;
        space.spaceStatus = SpaceStatus.ACTIVE;
        space.usageStatus = UsageStatus.VACANT;
        space.deleted = false;
        space.createdAt = LocalDateTime.now();
        space.updatedAt = LocalDateTime.now();
        space.createdBy = createdBy;
        space.updatedBy = createdBy;
        space.version = 0L;

        // 设置空间路径
        space.updateSpacePath(null);

        return space;
    }

    /**
     * 添加子空间
     */
    public void addChildSpace(Long childSpaceId) {
        // 验证层级关系
        if (this.spaceType == SpaceType.ROOM) {
            throw new IllegalStateException("房间不能添加子空间");
        }

        Integer childLevel = this.spaceLevel + 1;
        if (childLevel > 4) {
            throw new IllegalArgumentException("空间层级不能超过4层");
        }
    }

    /**
     * 更新空间路径
     */
    public void updateSpacePath(String parentPath) {
        if (parentPath == null) {
            this.spacePath = "/" + this.spaceCode;
            this.rootSpaceId = this.id;
        } else {
            this.spacePath = parentPath + "/" + this.spaceCode;
            // 从路径中提取根空间ID
            String[] parts = parentPath.split("/");
            if (parts.length > 1) {
                this.rootSpaceId = Long.parseLong(parts[1]);
            }
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置位置信息
     */
    public void setLocation(
            String address,
            String province,
            String city,
            String district,
            BigDecimal longitude,
            BigDecimal latitude,
            BigDecimal altitude) {
        this.address = address;
        this.province = province;
        this.city = city;
        this.district = district;
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.updatedAt = LocalDateTime.now();
        registerUpdateEvent();
    }

    /**
     * 设置边界
     */
    public void setBoundary(List<List<BigDecimal>> boundary, BigDecimal area) {
        this.boundary = boundary;
        this.area = area;
        this.updatedAt = LocalDateTime.now();
        registerUpdateEvent();
    }

    /**
     * 设置空间边界
     * 校验边界有效性并更新时间戳，同时注册边界更新事件
     *
     * @param spatialBounds 空间边界值对象
     * @throws IllegalArgumentException 如果边界无效
     */
    public void setSpatialBounds(SpatialBounds spatialBounds) {
        if (spatialBounds != null && !spatialBounds.isValid()) {
            throw new IllegalArgumentException("无效的空间边界：东北角坐标必须大于西南角坐标");
        }
        this.spatialBounds = spatialBounds;
        this.updatedAt = LocalDateTime.now();
        registerBoundsUpdatedEvent(spatialBounds);
    }

    /**
     * 判断坐标是否在空间边界内
     * 如果空间没有设置边界，则返回 false
     *
     * @param coordinate 要判断的坐标
     * @return 如果坐标在边界内返回 true，否则返回 false
     */
    public boolean containsCoordinate(Coordinate coordinate) {
        if (this.spatialBounds == null) {
            return false;
        }
        return this.spatialBounds.contains(coordinate);
    }

    /**
     * 注册边界更新事件
     */
    private void registerBoundsUpdatedEvent(SpatialBounds spatialBounds) {
        SpaceBoundsUpdatedEvent event = new SpaceBoundsUpdatedEvent(
                this.id,
                this.spaceCode,
                this.tenantId,
                spatialBounds,
                this.updatedAt,
                this.updatedBy
        );
        addDomainEvent(event);
    }

    /**
     * 设置楼层号
     */
    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置房间号
     */
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置容量
     */
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置空间名称
     */
    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置更新人ID
     */
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置父空间ID
     */
    public void setParentSpaceId(Long parentSpaceId) {
        this.parentSpaceId = parentSpaceId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新空间基本信息
     */
    public void updateBasicInfo(String spaceName, Integer capacity) {
        this.spaceName = spaceName;
        this.capacity = capacity;
        this.updatedAt = LocalDateTime.now();
        registerUpdateEvent();
    }

    /**
     * 注册空间更新事件
     */
    private void registerUpdateEvent() {
        SpaceUpdatedEvent event = new SpaceUpdatedEvent(
                this.id,
                this.spaceCode,
                this.spaceName,
                this.tenantId,
                this.spaceType,
                this.updatedAt,
                this.updatedBy
        );
        addDomainEvent(event);
    }

    /**
     * 更新使用状态
     */
    public void updateUsageStatus(UsageStatus usageStatus) {
        this.usageStatus = usageStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 停用空间
     */
    public void deactivate() {
        SpaceStatus previousStatus = this.spaceStatus;
        this.spaceStatus = SpaceStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
        registerStatusChangedEvent(previousStatus, this.spaceStatus, "空间停用");
    }

    /**
     * 激活空间
     */
    public void activate() {
        SpaceStatus previousStatus = this.spaceStatus;
        this.spaceStatus = SpaceStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
        registerStatusChangedEvent(previousStatus, this.spaceStatus, "空间激活");
    }

    /**
     * 进入维护状态
     */
    public void enterMaintenance() {
        SpaceStatus previousStatus = this.spaceStatus;
        this.spaceStatus = SpaceStatus.MAINTENANCE;
        this.updatedAt = LocalDateTime.now();
        registerStatusChangedEvent(previousStatus, this.spaceStatus, "进入维护");
    }

    /**
     * 注册状态变更事件
     */
    private void registerStatusChangedEvent(SpaceStatus previousStatus, SpaceStatus currentStatus, String reason) {
        SpaceStatusChangedEvent event = new SpaceStatusChangedEvent(
                this.id,
                this.spaceCode,
                this.tenantId,
                previousStatus,
                currentStatus,
                reason,
                this.updatedAt
        );
        addDomainEvent(event);
    }

    /**
     * 软删除
     */
    public void softDelete(Long deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.updatedAt = LocalDateTime.now();

        // 注册删除事件
        SpaceDeletedEvent event = new SpaceDeletedEvent(
                this.id,
                this.spaceCode,
                this.spaceName,
                this.tenantId,
                this.spaceType,
                this.deletedAt,
                deletedBy
        );
        addDomainEvent(event);
    }

    /**
     * 绑定资源到空间
     */
    public void bindResource(Long resourceId, String resourceCode, String reason, Long boundBy) {
        // 资源绑定逻辑由 SpaceResource 实体处理
        // 这里仅注册事件
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 从空间解绑资源
     */
    public void unbindResource(Long resourceId, String resourceCode, String reason, Long unboundBy) {
        // 资源解绑逻辑由 SpaceResource 实体处理
        // 这里仅注册事件
        this.updatedAt = LocalDateTime.now();
    }
}
