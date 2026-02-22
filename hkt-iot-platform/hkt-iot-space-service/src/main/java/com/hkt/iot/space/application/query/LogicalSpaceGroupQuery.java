package com.hkt.iot.space.application.query;

import lombok.Value;

/**
 * 逻辑空间分组查询对象
 *
 * @author HKT IoT Team
 */
@Value
public class LogicalSpaceGroupQuery {

    /**
     * 租户ID
     */
    Long tenantId;

    /**
     * 分组ID
     */
    Long groupId;

    /**
     * 分组编码
     */
    String groupCode;

    /**
     * 分组名称（模糊查询）
     */
    String groupName;

    /**
     * 分组类型
     */
    GroupTypeQuery groupType;

    /**
     * 分组状态
     */
    GroupStatusQuery status;

    /**
     * 关键字（编码或名称模糊查询）
     */
    String keyword;

    /**
     * 包含指定空间ID的分组
     */
    Long containsSpaceId;

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
     * 分组类型枚举
     */
    public enum GroupTypeQuery {
        APPLICATION, TENANT, BUSINESS
    }

    /**
     * 分组状态枚举
     */
    public enum GroupStatusQuery {
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
