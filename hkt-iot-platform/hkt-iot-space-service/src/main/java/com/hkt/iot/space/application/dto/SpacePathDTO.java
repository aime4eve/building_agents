package com.hkt.iot.space.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 空间路径DTO
 * 用于展示空间从根节点到当前节点的完整路径
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpacePathDTO {

    /**
     * 空间ID
     */
    private Long spaceId;

    /**
     * 空间编码
     */
    private String spaceCode;

    /**
     * 空间路径（如：/园区A/楼栋B/楼层C）
     */
    private String spacePath;

    /**
     * 路径节点列表（从根节点到当前节点的顺序）
     */
    private List<SpacePathNode> pathNodes;

    /**
     * 空间路径节点
     * 表示路径中的单个节点信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpacePathNode {

        /**
         * 空间ID
         */
        private Long spaceId;

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
         * 空间层级
         */
        private Integer spaceLevel;
    }
}
