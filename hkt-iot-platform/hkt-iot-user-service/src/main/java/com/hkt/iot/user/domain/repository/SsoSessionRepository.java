package com.hkt.iot.user.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.user.domain.model.SsoSession;

import java.util.List;
import java.util.Optional;

/**
 * SSO会话仓储接口
 * 基于DDD设计，提供SSO会话实体的持久化操作
 *
 * @author HKT IoT Team
 */
public interface SsoSessionRepository extends BaseRepository<SsoSession, Long> {

    /**
     * 根据会话ID查找
     *
     * @param sessionId 会话ID
     * @return 会话
     */
    Optional<SsoSession> findBySessionId(String sessionId);

    /**
     * 根据会话令牌查找
     *
     * @param sessionToken 会话令牌
     * @return 会话
     */
    Optional<SsoSession> findBySessionToken(String sessionToken);

    /**
     * 根据用户ID查找会话
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<SsoSession> findByUserId(Long userId);

    /**
     * 根据租户ID查找会话
     *
     * @param tenantId 租户ID
     * @return 会话列表
     */
    List<SsoSession> findByTenantId(Long tenantId);

    /**
     * 根据用户ID查找活跃会话
     *
     * @param userId 用户ID
     * @return 活跃会话列表
     */
    List<SsoSession> findActiveByUserId(Long userId);

    /**
     * 根据客户端ID查找会话
     *
     * @param clientId 客户端ID
     * @return 会话列表
     */
    List<SsoSession> findByClientId(String clientId);

    /**
     * 查找过期的会话
     *
     * @return 过期会话列表
     */
    List<SsoSession> findExpired();

    /**
     * 删除用户的所有会话
     *
     * @param userId 用户ID
     */
    void deleteByUserId(Long userId);

    /**
     * 统计用户的活跃会话数量
     *
     * @param userId 用户ID
     * @return 活跃会话数量
     */
    long countActiveByUserId(Long userId);

    /**
     * 删除过期会话
     */
    void deleteExpired();
}
