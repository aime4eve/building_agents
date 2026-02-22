package com.hkt.iot.space.application.query;

import lombok.Value;

import java.time.LocalDateTime;

/**
 * 空间资源查询对象
 *
 * @author HKT IoT Team
 */
@Value
public class SpaceResourceQuery {

    /**
     * 租户ID
     */
    Long tenantId;

    /**
     * 空间资源关联ID
     */
    Long spaceResourceId;

    /**
     * 空间ID
     */
    Long spaceId;

    /**
     * 空间编码
     */
    String spaceCode;

    /**
     * 资源类型
     */
    ResourceTypeQuery resourceType;

    /**
     * 资源ID
     */
    Long resourceId;

    /**
     * 资源编码
     */
    String resourceCode;

    /**
     * 关联类型
     */
    RelationTypeQuery relationType;

    /**
     * 是否主关联
     */
    Boolean primaryRelation;

    /**
     * 资源状态
     */
    ResourceStatusQuery status;

    /**
     * 生效开始日期（起始）
     */
    LocalDateTime startDateFrom;

    /**
     * 生效开始日期（结束）
     */
    LocalDateTime startDateTo;

    /**
     * 生效结束日期（起始）
     */
    LocalDateTime endDateFrom;

    /**
     * 生效结束日期（结束）
     */
    LocalDateTime endDateTo;

    /**
     * 只查询有效关联（在有效期内且状态为激活）
     */
    Boolean validOnly;

    /**
     * 页码（从1开始）
     */
    Integer pageNum;

    /**
     * 每页条数
     */
    Integer pageSize;

    /**
     * 排序字段
     */
    String sortField;

    /**
     * 排序方向（ASC/DESC）
     */
    String sortOrder;

    /**
     * 资源类型枚举
     */
    public enum ResourceTypeQuery {
        DEVICE, USER, ASSET, EQUIPMENT
    }

    /**
     * 关联类型枚举
     */
    public enum RelationTypeQuery {
        OWNER, OCCUPANT, MANAGER, TEMPORARY
    }

    /**
     * 资源状态枚举
     */
    public enum ResourceStatusQuery {
        ACTIVE, INACTIVE
    }

    /**
     * 获取默认分页参数
     */
    public Integer getPageNum() {
        return pageNum != null && pageNum > 0 ? pageNum : 1;
    }

    /**
     * 获取默认每页条数
     */
    public Integer getPageSize() {
        return pageSize != null && pageSize > 0 ? pageSize : 10;
    }
}
