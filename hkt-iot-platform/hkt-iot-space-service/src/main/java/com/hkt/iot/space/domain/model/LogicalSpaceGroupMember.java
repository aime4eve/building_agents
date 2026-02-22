package com.hkt.iot.space.domain.model;

import com.hkt.iot.domain.model.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 逻辑空间组成员实体
 * 基于DDL: logical_space_group_member表
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "logical_space_group_member", uniqueConstraints = {
    @UniqueConstraint(name = "uk_group_space_member", columnNames = {"group_id", "space_id", "deleted"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogicalSpaceGroupMember extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "group_code", nullable = false, length = 100)
    private String groupCode;

    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    @Column(name = "space_code", nullable = false, length = 100)
    private String spaceCode;

    @Column(name = "space_name", length = 200)
    private String spaceName;

    @Column(name = "member_order")
    private Integer memberOrder;

    @Column(name = "is_pinned")
    private Boolean isPinned;

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
     * 工厂方法：创建分组成员
     */
    public static LogicalSpaceGroupMember create(
            Long tenantId,
            Long groupId,
            String groupCode,
            Long spaceId,
            String spaceCode,
            String spaceName,
            Long createdBy) {
        LogicalSpaceGroupMember member = new LogicalSpaceGroupMember();
        member.tenantId = tenantId;
        member.groupId = groupId;
        member.groupCode = groupCode;
        member.spaceId = spaceId;
        member.spaceCode = spaceCode;
        member.spaceName = spaceName;
        member.memberOrder = 0;
        member.isPinned = false;
        member.deleted = false;
        member.createdAt = LocalDateTime.now();
        member.updatedAt = LocalDateTime.now();
        member.createdBy = createdBy;
        member.updatedBy = createdBy;
        return member;
    }

    /**
     * 更新成员顺序
     */
    public void updateMemberOrder(Integer memberOrder) {
        this.memberOrder = memberOrder;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 置顶
     */
    public void pin() {
        this.isPinned = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 取消置顶
     */
    public void unpin() {
        this.isPinned = false;
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
