package com.hkt.iot.space.application.dto;

import com.hkt.iot.space.domain.model.SpaceResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 空间资源关联DTO
 * 用于空间资源数据的传输
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceResourceDTO {

    /**
     * 关联ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 空间ID
     */
    private Long spaceId;

    /**
     * 空间编码
     */
    private String spaceCode;

    /**
     * 资源类型
     */
    private ResourceTypeDTO resourceType;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 资源编码
     */
    private String resourceCode;

    /**
     * 关联类型
     */
    private RelationTypeDTO relationType;

    /**
     * 是否主关联
     */
    private Boolean primaryRelation;

    /**
     * 位置详情
     */
    private String locationDetail;

    /**
     * 楼层号
     */
    private Integer floorNumber;

    /**
     * 房间号
     */
    private String roomNumber;

    /**
     * 生效开始日期
     */
    private LocalDateTime startDate;

    /**
     * 生效结束日期
     */
    private LocalDateTime endDate;

    /**
     * 状态
     */
    private ResourceStatusDTO status;

    /**
     * 扩展属性
     */
    private Map<String, Object> extProperties;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 资源类型枚举
     */
    public enum ResourceTypeDTO {
        DEVICE, USER, ASSET, EQUIPMENT
    }

    /**
     * 关联类型枚举
     */
    public enum RelationTypeDTO {
        OWNER, OCCUPANT, MANAGER, TEMPORARY
    }

    /**
     * 资源状态枚举
     */
    public enum ResourceStatusDTO {
        ACTIVE, INACTIVE
    }

    /**
     * 从领域模型转换为DTO
     *
     * @param spaceResource 空间资源领域模型
     * @return 空间资源DTO
     */
    public static SpaceResourceDTO from(SpaceResource spaceResource) {
        return SpaceResourceDTO.builder()
                .id(spaceResource.getId())
                .tenantId(spaceResource.getTenantId())
                .spaceId(spaceResource.getSpaceId())
                .spaceCode(spaceResource.getSpaceCode())
                .resourceType(convertResourceType(spaceResource.getResourceType()))
                .resourceId(spaceResource.getResourceId())
                .resourceCode(spaceResource.getResourceCode())
                .relationType(convertRelationType(spaceResource.getRelationType()))
                .primaryRelation(spaceResource.getPrimaryRelation())
                .locationDetail(spaceResource.getLocationDetail())
                .floorNumber(spaceResource.getFloorNumber())
                .roomNumber(spaceResource.getRoomNumber())
                .startDate(spaceResource.getStartDate())
                .endDate(spaceResource.getEndDate())
                .status(convertResourceStatus(spaceResource.getStatus()))
                .extProperties(spaceResource.getExtProperties())
                .createdAt(spaceResource.getCreatedAt())
                .updatedAt(spaceResource.getUpdatedAt())
                .createdBy(spaceResource.getCreatedBy())
                .updatedBy(spaceResource.getUpdatedBy())
                .build();
    }

    /**
     * 转换资源类型枚举
     */
    private static ResourceTypeDTO convertResourceType(SpaceResource.ResourceType resourceType) {
        if (resourceType == null) {
            return null;
        }
        return ResourceTypeDTO.valueOf(resourceType.name());
    }

    /**
     * 转换关联类型枚举
     */
    private static RelationTypeDTO convertRelationType(SpaceResource.RelationType relationType) {
        if (relationType == null) {
            return null;
        }
        return RelationTypeDTO.valueOf(relationType.name());
    }

    /**
     * 转换资源状态枚举
     */
    private static ResourceStatusDTO convertResourceStatus(SpaceResource.ResourceStatus resourceStatus) {
        if (resourceStatus == null) {
            return null;
        }
        return ResourceStatusDTO.valueOf(resourceStatus.name());
    }
}
