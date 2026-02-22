package com.hkt.iot.space.interfaces.rest.dto;

import com.hkt.iot.space.domain.model.SpaceResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 空间资源响应DTO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "空间资源响应")
public class SpaceResourceResponse {

    @Schema(description = "关联ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "空间ID")
    private Long spaceId;

    @Schema(description = "空间编码")
    private String spaceCode;

    @Schema(description = "资源类型")
    private SpaceResource.ResourceType resourceType;

    @Schema(description = "资源ID")
    private Long resourceId;

    @Schema(description = "资源编码")
    private String resourceCode;

    @Schema(description = "关联类型")
    private SpaceResource.RelationType relationType;

    @Schema(description = "是否主关联")
    private Boolean primaryRelation;

    @Schema(description = "位置详情")
    private String locationDetail;

    @Schema(description = "楼层号")
    private Integer floorNumber;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "生效开始日期")
    private LocalDateTime startDate;

    @Schema(description = "生效结束日期")
    private LocalDateTime endDate;

    @Schema(description = "扩展属性")
    private Map<String, Object> extProperties;

    @Schema(description = "关联状态")
    private SpaceResource.ResourceStatus status;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "更新人ID")
    private Long updatedBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "创建人ID")
    private Long createdBy;

    /**
     * 从领域模型转换为Response
     *
     * @param spaceResource 空间资源领域模型
     * @return 空间资源响应DTO
     */
    public static SpaceResourceResponse from(SpaceResource spaceResource) {
        return SpaceResourceResponse.builder()
                .id(spaceResource.getId())
                .tenantId(spaceResource.getTenantId())
                .spaceId(spaceResource.getSpaceId())
                .spaceCode(spaceResource.getSpaceCode())
                .resourceType(spaceResource.getResourceType())
                .resourceId(spaceResource.getResourceId())
                .resourceCode(spaceResource.getResourceCode())
                .relationType(spaceResource.getRelationType())
                .primaryRelation(spaceResource.getPrimaryRelation())
                .locationDetail(spaceResource.getLocationDetail())
                .floorNumber(spaceResource.getFloorNumber())
                .roomNumber(spaceResource.getRoomNumber())
                .startDate(spaceResource.getStartDate())
                .endDate(spaceResource.getEndDate())
                .status(spaceResource.getStatus())
                .extProperties(spaceResource.getExtProperties())
                .updatedAt(spaceResource.getUpdatedAt())
                .updatedBy(spaceResource.getUpdatedBy())
                .createdAt(spaceResource.getCreatedAt())
                .createdBy(spaceResource.getCreatedBy())
                .build();
    }
}
