package com.hkt.iot.space.domain.service;

import com.hkt.iot.space.domain.event.SpaceResourceBoundEvent;
import com.hkt.iot.space.domain.event.SpaceResourceUnboundEvent;
import com.hkt.iot.space.domain.model.Space;
import com.hkt.iot.space.domain.model.SpaceResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 空间资源领域服务
 * 处理资源绑定和解绑的业务逻辑
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
public class SpaceResourceDomainService {

    /**
     * 绑定资源到空间
     * 创建 SpaceResource 关联并注册事件
     *
     * @param space         空间聚合根
     * @param resourceType  资源类型
     * @param resourceId    资源ID
     * @param resourceCode  资源编码
     * @param relationType  关联类型
     * @param boundBy       操作人ID
     * @return 空间资源关联
     */
    public SpaceResource bindResource(
            Space space,
            SpaceResource.ResourceType resourceType,
            Long resourceId,
            String resourceCode,
            SpaceResource.RelationType relationType,
            Long boundBy) {

        // 创建空间资源关联
        SpaceResource spaceResource = SpaceResource.create(
                space.getTenantId(),
                space.getId(),
                space.getSpaceCode(),
                resourceType,
                resourceId,
                relationType,
                boundBy
        );

        // 注册资源绑定事件到空间聚合根
        SpaceResourceBoundEvent event = new SpaceResourceBoundEvent(
                space.getId(),
                space.getSpaceCode(),
                space.getTenantId(),
                resourceType,
                resourceId,
                resourceCode,
                relationType,
                LocalDateTime.now(),
                boundBy
        );
        space.addDomainEvent(event);

        log.info("资源绑定成功: spaceId={}, resourceId={}, resourceType={}",
                space.getId(), resourceId, resourceType);

        return spaceResource;
    }

    /**
     * 解绑资源
     * 注册解绑事件
     *
     * @param space          空间聚合根
     * @param spaceResource  空间资源关联
     * @param unboundReason  解绑原因
     * @param unboundBy      操作人ID
     */
    public void unbindResource(
            Space space,
            SpaceResource spaceResource,
            String unboundReason,
            Long unboundBy) {

        // 注册资源解绑事件到空间聚合根
        SpaceResourceUnboundEvent event = new SpaceResourceUnboundEvent(
                space.getId(),
                space.getSpaceCode(),
                space.getTenantId(),
                spaceResource.getResourceType(),
                spaceResource.getResourceId(),
                spaceResource.getResourceCode(),
                unboundReason,
                LocalDateTime.now(),
                unboundBy
        );
        space.addDomainEvent(event);

        log.info("资源解绑成功: spaceId={}, resourceId={}, resourceType={}",
                space.getId(), spaceResource.getResourceId(), spaceResource.getResourceType());
    }

    /**
     * 验证资源是否可以绑定到空间
     *
     * @param space        空间
     * @param resourceType 资源类型
     * @param resourceId   资源ID
     * @return 是否可以绑定
     */
    public boolean canBindResource(Space space, SpaceResource.ResourceType resourceType, Long resourceId) {
        // 检查空间状态
        if (space.getSpaceStatus() == Space.SpaceStatus.INACTIVE) {
            log.warn("空间已停用，无法绑定资源: spaceId={}", space.getId());
            return false;
        }

        if (space.getSpaceStatus() == Space.SpaceStatus.MAINTENANCE) {
            log.warn("空间维护中，无法绑定资源: spaceId={}", space.getId());
            return false;
        }

        // 可以添加更多业务验证逻辑
        return true;
    }

    /**
     * 验证资源是否可以解绑
     *
     * @param space         空间
     * @param spaceResource 空间资源关联
     * @return 是否可以解绑
     */
    public boolean canUnbindResource(Space space, SpaceResource spaceResource) {
        // 检查关联是否有效
        if (!spaceResource.isValid()) {
            log.warn("资源关联已无效，无需解绑: spaceResource={}", spaceResource.getId());
            return false;
        }

        // 可以添加更多业务验证逻辑，例如检查是否是主关联等
        return true;
    }
}
