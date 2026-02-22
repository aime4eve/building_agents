package com.hkt.iot.space.application.command;

import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 绑定资源命令对象
 *
 * @author HKT IoT Team
 */
@Value
public class BindResourceCommand {

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
     * 资源类型
     */
    ResourceTypeCommand resourceType;

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
    RelationTypeCommand relationType;

    /**
     * 是否主关联
     */
    Boolean primaryRelation;

    /**
     * 位置详情
     */
    String locationDetail;

    /**
     * 楼层号
     */
    Integer floorNumber;

    /**
     * 房间号
     */
    String roomNumber;

    /**
     * 生效开始日期
     */
    LocalDateTime startDate;

    /**
     * 生效结束日期
     */
    LocalDateTime endDate;

    /**
     * 扩展属性
     */
    Map<String, Object> extProperties;

    /**
     * 创建人ID
     */
    Long createdBy;

    /**
     * 资源类型枚举
     */
    public enum ResourceTypeCommand {
        DEVICE, USER, ASSET, EQUIPMENT
    }

    /**
     * 关联类型枚举
     */
    public enum RelationTypeCommand {
        OWNER, OCCUPANT, MANAGER, TEMPORARY
    }

    /**
     * 验证命令对象
     */
    public void validate() {
        if (tenantId == null) {
            throw new IllegalArgumentException("租户ID不能为空");
        }
        if (spaceId == null) {
            throw new IllegalArgumentException("空间ID不能为空");
        }
        if (spaceCode == null || spaceCode.trim().isEmpty()) {
            throw new IllegalArgumentException("空间编码不能为空");
        }
        if (resourceType == null) {
            throw new IllegalArgumentException("资源类型不能为空");
        }
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为空");
        }
        if (relationType == null) {
            throw new IllegalArgumentException("关联类型不能为空");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("创建人ID不能为空");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
    }
}
