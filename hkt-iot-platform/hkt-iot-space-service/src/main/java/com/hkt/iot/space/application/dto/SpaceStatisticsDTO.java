package com.hkt.iot.space.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 空间统计DTO
 * 用于展示租户下空间的统计信息
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpaceStatisticsDTO {

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 空间总数
     */
    private Long totalSpaces;

    /**
     * 按类型统计数量
     * key: 空间类型（PARK/BUILDING/FLOOR/ROOM）
     * value: 数量
     */
    private Map<String, Long> countByType;

    /**
     * 按状态统计数量
     * key: 空间状态（ACTIVE/INACTIVE/MAINTENANCE）
     * value: 数量
     */
    private Map<String, Long> countByStatus;

    /**
     * 按层级统计数量
     * key: 空间层级（1-4）
     * value: 数量
     */
    private Map<Integer, Long> countByLevel;
}
