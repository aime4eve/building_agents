package com.hkt.iot.notification.domain.repository;

import com.hkt.iot.notification.domain.model.NotificationTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 通知模板仓储接口
 *
 * @author HKT IoT Team
 */
@Repository
public interface NotificationTemplateRepository {

    /**
     * 保存模板
     */
    NotificationTemplate save(NotificationTemplate template);

    /**
     * 根据ID查找模板
     */
    Optional<NotificationTemplate> findById(Long id);

    /**
     * 根据模板编码查找模板
     */
    Optional<NotificationTemplate> findByTemplateCode(String templateCode);

    /**
     * 根据租户ID和模板编码查找模板
     */
    Optional<NotificationTemplate> findByTenantIdAndTemplateCode(String tenantId, String templateCode);

    /**
     * 查找租户的所有模板
     */
    List<NotificationTemplate> findByTenantId(String tenantId);

    /**
     * 根据类型和渠道查找启用的模板
     */
    List<NotificationTemplate> findEnabledByTypeAndChannel(
            NotificationTemplate.TemplateType templateType,
            NotificationTemplate.ChannelType channelType
    );

    /**
     * 删除模板
     */
    void deleteById(Long id);

    /**
     * 检查模板编码是否存在
     */
    boolean existsByTemplateCode(String templateCode);

    /**
     * 检查模板编码是否存在（排除指定ID）
     */
    boolean existsByTemplateCodeAndIdNot(String templateCode, Long id);
}
