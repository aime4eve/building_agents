package com.hkt.iot.space.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 空间拓扑DTO
 * 用于展示空间的层级结构拓扑关系
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceTopologyDTO {

    /**
     * 空间ID
     */
    private Long id;

    /**
     * 空间编码
     */
    private String spaceCode;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间类型
     */
    private String spaceType;

    /**
     * 空间状态
     */
    private String spaceStatus;

    /**
     * 父空间ID
     */
    private Long parentSpaceId;

    /**
     * 父空间名称
     */
    private String parentSpaceName;

    /**
     * 空间层级
     */
    private Integer spaceLevel;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 子空间列表
     */
    private List<SpaceTopologyDTO> children;

    /**
     * 子空间数量
     */
    private Integer childCount;
}
