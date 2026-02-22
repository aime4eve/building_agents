package com.hkt.iot.user.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.user.domain.model.User;
import com.hkt.iot.user.domain.model.User.UserStatus;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口
 * 基于DDD设计，提供用户实体的持久化操作
 *
 * @author HKT IoT Team
 */
public interface UserRepository extends BaseRepository<User, Long> {

    /**
     * 根据租户ID和用户名查找
     *
     * @param tenantId 租户ID
     * @param username 用户名
     * @return 用户
     */
    Optional<User> findByTenantIdAndUsername(Long tenantId, String username);

    /**
     * 根据邮箱查找
     *
     * @param email 邮箱
     * @return 用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查找
     *
     * @param phone 手机号
     * @return 用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 根据租户ID查找所有用户
     *
     * @param tenantId 租户ID
     * @return 用户列表
     */
    List<User> findByTenantId(Long tenantId);

    /**
     * 根据用户状态查找
     *
     * @param userStatus 用户状态
     * @return 用户列表
     */
    List<User> findByUserStatus(UserStatus userStatus);

    /**
     * 根据租户ID和用户状态查找
     *
     * @param tenantId   租户ID
     * @param userStatus 用户状态
     * @return 用户列表
     */
    List<User> findByTenantIdAndUserStatus(Long tenantId, UserStatus userStatus);

    /**
     * 检查用户名是否存在
     *
     * @param tenantId 租户ID
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByTenantIdAndUsername(Long tenantId, String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 统计租户下的用户数量
     *
     * @param tenantId 租户ID
     * @return 用户数量
     */
    long countByTenantId(Long tenantId);

    /**
     * 根据租户ID分页查询用户
     *
     * @param tenantId 租户ID
     * @param page     页码
     * @param size     每页大小
     * @return 用户列表
     */
    List<User> findByTenantIdPaging(Long tenantId, int page, int size);
}
