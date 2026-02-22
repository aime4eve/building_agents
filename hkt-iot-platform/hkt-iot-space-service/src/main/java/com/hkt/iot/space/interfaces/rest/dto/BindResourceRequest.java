package com.hkt.iot.space.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 绑定资源请求DTO
 *
 * @author HKT IoT Team
 */
@Data
@Schema(description = "绑定资源请求")
public class BindResourceRequest {

    @Schema(description = "租户ID", required = true)
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    @Schema(description = "空间编码", required = true)
    @NotBlank(message = "空间编码不能为空")
    private String spaceCode;

    @Schema(description = "资源类型", required = true)
    @NotNull(message = "资源类型不能为空")
    private ResourceType resourceType;

    @Schema(description = "资源ID", required = true)
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    @Schema(description = "资源编码", example = "DEV001")
    private String resourceCode;

    @Schema(description = "关联类型", required = true)
    @NotNull(message = "关联类型不能为空")
    private RelationType relationType;

    @Schema(description = "是否主关联", example = "true")
    private Boolean primaryRelation = false;

    @Schema(description = "位置详情", example = "东侧机房")
    private String locationDetail;

    @Schema(description = "楼层号", example = "5")
    private Integer floorNumber;

    @Schema(description = "房间号", example = "501")
    private String roomNumber;

    @Schema(description = "生效开始日期")
    private LocalDateTime startDate;

    @Schema(description = "生效结束日期")
    private LocalDateTime endDate;

    @Schema(description = "扩展属性")
    private Map<String, Object> extProperties;

    /**
     * 资源类型枚举
     */
    public enum ResourceType {
        @Schema(description = "设备")
        DEVICE,
        @Schema(description = "用户")
        USER,
        @Schema(description = "资产")
        ASSET,
        @Schema(description = "装备")
        EQUIPMENT
    }

    /**
     * 关联类型枚举
     */
    public enum RelationType {
        @Schema(description = "所有者")
        OWNER,
        @Schema(description = "占用者")
        OCCUPANT,
        @Schema(description = "管理者")
        MANAGER,
        @Schema(description = "临时")
        TEMPORARY
    }
}
