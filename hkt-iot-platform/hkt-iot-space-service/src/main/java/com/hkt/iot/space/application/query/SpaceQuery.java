package com.hkt.iot.space.application.query;

import lombok.Value;

/**
 * 空间查询对象
 *
 * @author HKT IoT Team
 */
@Value
public class SpaceQuery {

    /**
     * 租户ID
     */
    Long tenantId;

    /**
     * 空间ID
     */
    Long spaceId;

    /**
     * 空间编码
     */
    String spaceCode;

    /**
     * 空间名称（模糊查询）
     */
    String spaceName;

    /**
     * 空间类型
     */
    SpaceTypeQuery spaceType;

    /**
     * 空间状态
     */
    SpaceStatusQuery spaceStatus;

    /**
     * 使用状态
     */
    UsageStatusQuery usageStatus;

    /**
     * 父空间ID
     */
    Long parentSpaceId;

    /**
     * 根空间ID
     */
    Long rootSpaceId;

    /**
     * 空间层级
     */
    Integer spaceLevel;

    /**
     * 省份
     */
    String province;

    /**
     * 城市
     */
    String city;

    /**
     * 区县
     */
    String district;

    /**
     * 关键字（编码或名称模糊查询）
     */
    String keyword;

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
     * 空间类型枚举
     */
    public enum SpaceTypeQuery {
        PARK, BUILDING, FLOOR, ROOM
    }

    /**
     * 空间状态枚举
     */
    public enum SpaceStatusQuery {
        ACTIVE, INACTIVE, MAINTENANCE
    }

    /**
     * 使用状态枚举
     */
    public enum UsageStatusQuery {
        OCCUPIED, VACANT, RESERVED
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
