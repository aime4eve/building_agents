package com.hkt.iot.space.interfaces.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 空间树节点DTO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "空间树节点")
public class SpaceTreeNode {

    @Schema(description = "空间ID")
    private Long id;

    @Schema(description = "空间编码")
    private String spaceCode;

    @Schema(description = "空间名称")
    private String spaceName;

    @Schema(description = "空间类型")
    private SpaceType spaceType;

    @Schema(description = "空间层级")
    private Integer spaceLevel;

    @Schema(description = "父空间ID")
    private Long parentSpaceId;

    @Schema(description = "子空间列表")
    private List<SpaceTreeNode> children;

    /**
     * 空间类型枚举
     */
    public enum SpaceType {
        PARK, BUILDING, FLOOR, ROOM
    }
}
