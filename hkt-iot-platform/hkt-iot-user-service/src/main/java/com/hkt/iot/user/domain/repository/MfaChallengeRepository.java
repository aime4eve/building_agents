package com.hkt.iot.user.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.user.domain.model.MfaChallenge;

import java.util.List;
import java.util.Optional;

/**
 * MFA挑战仓储接口
 * 基于DDD设计，提供MFA挑战实体的持久化操作
 *
 * @author HKT IoT Team
 */
public interface MfaChallengeRepository extends BaseRepository<MfaChallenge, Long> {

    /**
     * 根据挑战码查找
     *
     * @param challengeCode 挑战码
     * @return MFA挑战
     */
    Optional<MfaChallenge> findByChallengeCode(String challengeCode);

    /**
     * 根据用户ID查找挑战
     *
     * @param userId 用户ID
     * @return 挑战列表
     */
    List<MfaChallenge> findByUserId(Long userId);

    /**
     * 根据用户ID和状态查找
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 挑战列表
     */
    List<MfaChallenge> findByUserIdAndStatus(Long userId, MfaChallenge.ChallengeStatus status);

    /**
     * 查找过期的挑战
     *
     * @return 过期挑战列表
     */
    List<MfaChallenge> findExpired();

    /**
     * 删除过期的挑战
     */
    void deleteExpired();

    /**
     * 统计用户的待处理挑战数量
     *
     * @param userId 用户ID
     * @return 待处理挑战数量
     */
    long countPendingByUserId(Long userId);
}
