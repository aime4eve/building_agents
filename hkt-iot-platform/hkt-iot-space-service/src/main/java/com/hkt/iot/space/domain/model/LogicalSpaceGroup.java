package com.hkt.iot.space.domain.model;

import com.hkt.iot.domain.model.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 逻辑空间分组聚合根
 * 基于DDL: logical_space_group表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "logical_space_group", uniqueConstraints = {
    @UniqueConstraint(name = "uk_tenant_group_code", columnNames = {"tenant_id", "group_code", "deleted"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogicalSpaceGroup extends AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "group_code", nullable = false, length = 100)
    private String groupCode;

    @Column(name = "group_name", nullable = false, length = 200)
    private String groupName;

    @Column(name = "group_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private GroupType groupType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "group_color", length = 20)
    private String groupColor;

    @Column(name = "group_icon", length = 200)
    private String groupIcon;

    @Column(name = "group_rule", columnDefinition = "JSON")
    @Transient
    private Map<String, Object> groupRule;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private GroupStatus status;

    @Column(name = "display_order")
    private Integer displayOrder;

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
     * 分组类型
     */
    public enum GroupType {
        APPLICATION, TENANT, BUSINESS
    }

    /**
     * 分组状态
     */
    public enum GroupStatus {
        ACTIVE, INACTIVE
    }

    /**
     * 工厂方法：创建逻辑空间分组
     */
    public static LogicalSpaceGroup create(
            Long tenantId,
            String groupCode,
            String groupName,
            GroupType groupType,
            String description,
            Long createdBy) {
        LogicalSpaceGroup group = new LogicalSpaceGroup();
        group.tenantId = tenantId;
        group.groupCode = groupCode;
        group.groupName = groupName;
        group.groupType = groupType;
        group.description = description;
        group.status = GroupStatus.ACTIVE;
        group.displayOrder = 0;
        group.deleted = false;
        group.createdAt = LocalDateTime.now();
        group.updatedAt = LocalDateTime.now();
        group.createdBy = createdBy;
        group.updatedBy = createdBy;
        group.version = 0L;
        return group;
    }

    /**
     * 更新分组信息
     */
    public void updateGroupInfo(String groupName, String description, String groupColor, String groupIcon) {
        this.groupName = groupName;
        this.description = description;
        this.groupColor = groupColor;
        this.groupIcon = groupIcon;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新分组规则
     */
    public void updateGroupRule(Map<String, Object> groupRule) {
        this.groupRule = groupRule;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 激活
     */
    public void activate() {
        this.status = GroupStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 停用
     */
    public void deactivate() {
        this.status = GroupStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新显示顺序
     */
    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
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
}
