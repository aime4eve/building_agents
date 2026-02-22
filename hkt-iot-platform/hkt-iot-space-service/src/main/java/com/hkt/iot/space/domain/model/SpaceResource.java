package com.hkt.iot.space.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 空间资源关联实体
 * 基于DDL: space_resource表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "space_resource", uniqueConstraints = {
    @UniqueConstraint(name = "uk_space_resource", columnNames = {"space_id", "resource_type", "resource_id", "deleted"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpaceResource extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    @Column(name = "space_code", nullable = false, length = 100)
    private String spaceCode;

    @Column(name = "resource_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Column(name = "resource_code", length = 100)
    private String resourceCode;

    @Column(name = "relation_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RelationType relationType;

    @Column(name = "primary_relation", nullable = false)
    private Boolean primaryRelation;

    @Column(name = "location_detail", length = 500)
    private String locationDetail;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(name = "room_number", length = 50)
    private String roomNumber;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ResourceStatus status;

    @Column(name = "ext_properties", columnDefinition = "JSON")
    @Transient
    private Map<String, Object> extProperties;

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
     * 资源类型
     */
    public enum ResourceType {
        DEVICE, USER, ASSET, EQUIPMENT
    }

    /**
     * 关联类型
     */
    public enum RelationType {
        OWNER, OCCUPANT, MANAGER, TEMPORARY
    }

    /**
     * 资源状态
     */
    public enum ResourceStatus {
        ACTIVE, INACTIVE
    }

    /**
     * 工厂方法：创建空间资源关联
     */
    public static SpaceResource create(
            Long tenantId,
            Long spaceId,
            String spaceCode,
            ResourceType resourceType,
            Long resourceId,
            RelationType relationType,
            Long createdBy) {
        SpaceResource spaceResource = new SpaceResource();
        spaceResource.tenantId = tenantId;
        spaceResource.spaceId = spaceId;
        spaceResource.spaceCode = spaceCode;
        spaceResource.resourceType = resourceType;
        spaceResource.resourceId = resourceId;
        spaceResource.relationType = relationType;
        spaceResource.primaryRelation = false;
        spaceResource.status = ResourceStatus.ACTIVE;
        spaceResource.deleted = false;
        spaceResource.createdAt = LocalDateTime.now();
        spaceResource.updatedAt = LocalDateTime.now();
        spaceResource.createdBy = createdBy;
        spaceResource.updatedBy = createdBy;
        return spaceResource;
    }

    /**
     * 设置位置详情
     */
    public void setLocationDetail(String locationDetail, Integer floorNumber, String roomNumber) {
        this.locationDetail = locationDetail;
        this.floorNumber = floorNumber;
        this.roomNumber = roomNumber;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置资源编码
     */
    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设置生效时间
     */
    public void setDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 激活
     */
    public void activate() {
        this.status = ResourceStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 停用
     */
    public void deactivate() {
        this.status = ResourceStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 设为主关联
     */
    public void setAsPrimary() {
        this.primaryRelation = true;
        this.updatedAt = LocalDateTime.now();
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

    /**
     * 检查是否在有效期内
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        if (this.status != ResourceStatus.ACTIVE) {
            return false;
        }
        if (this.startDate != null && now.isBefore(this.startDate)) {
            return false;
        }
        if (this.endDate != null && now.isAfter(this.endDate)) {
            return false;
        }
        return true;
    }
}
