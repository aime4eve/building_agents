package com.hkt.iot.space.infrastructure.persistence;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.space.domain.model.LogicalSpaceGroupMember;
import com.hkt.iot.space.domain.repository.LogicalSpaceGroupMemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 逻辑空间组成员Repository实现
 * 基于DDL: logical_space_group_member表
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
public class LogicalSpaceGroupMemberRepositoryImpl implements LogicalSpaceGroupMemberRepository, BaseRepository<LogicalSpaceGroupMember, Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public LogicalSpaceGroupMember save(LogicalSpaceGroupMember member) {
        if (member.getId() == null) {
            entityManager.persist(member);
            log.debug("Created new logical space group member: id={}, groupId={}, spaceId={}",
                    member.getId(), member.getGroupId(), member.getSpaceId());
            return member;
        } else {
            LogicalSpaceGroupMember merged = entityManager.merge(member);
            log.debug("Updated logical space group member: id={}, groupId={}, spaceId={}",
                    merged.getId(), merged.getGroupId(), merged.getSpaceId());
            return merged;
        }
    }

    @Override
    public List<LogicalSpaceGroupMember> saveAll(List<LogicalSpaceGroupMember> entities) {
        return entities.stream()
                .map(this::save)
                .toList();
    }

    @Override
    public Optional<LogicalSpaceGroupMember> findById(Long id) {
        LogicalSpaceGroupMember member = entityManager.find(LogicalSpaceGroupMember.class, id);
        if (member != null && !member.getDeleted()) {
            return Optional.of(member);
        }
        return Optional.empty();
    }

    @Override
    public List<LogicalSpaceGroupMember> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        TypedQuery<LogicalSpaceGroupMember> query = entityManager.createQuery(
                "SELECT m FROM LogicalSpaceGroupMember m WHERE m.id IN :ids AND m.deleted = false",
                LogicalSpaceGroupMember.class
        );
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    @Override
    public List<LogicalSpaceGroupMember> findAll() {
        TypedQuery<LogicalSpaceGroupMember> query = entityManager.createQuery(
                "SELECT m FROM LogicalSpaceGroupMember m WHERE m.deleted = false ORDER BY m.createdAt DESC",
                LogicalSpaceGroupMember.class
        );
        return query.getResultList();
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    @Override
    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(m) FROM LogicalSpaceGroupMember m WHERE m.deleted = false",
                Long.class
        );
        return query.getSingleResult();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    @Transactional
    public void delete(LogicalSpaceGroupMember member) {
        member.setDeleted(true);
        member.setDeletedAt(java.time.LocalDateTime.now());
        entityManager.merge(member);
        log.debug("Soft deleted logical space group member: id={}", member.getId());
    }

    @Override
    @Transactional
    public void deleteAll(List<LogicalSpaceGroupMember> entities) {
        entities.forEach(this::delete);
    }

    @Override
    @Transactional
    public void deleteAll() {
        List<LogicalSpaceGroupMember> allMembers = findAll();
        allMembers.forEach(this::delete);
    }

    @Override
    public List<LogicalSpaceGroupMember> findByGroupId(Long groupId) {
        TypedQuery<LogicalSpaceGroupMember> query = entityManager.createQuery(
                "SELECT m FROM LogicalSpaceGroupMember m WHERE m.groupId = :groupId AND m.deleted = false ORDER BY m.memberOrder, m.createdAt DESC",
                LogicalSpaceGroupMember.class
        );
        query.setParameter("groupId", groupId);
        return query.getResultList();
    }

    @Override
    public List<LogicalSpaceGroupMember> findBySpaceId(Long spaceId) {
        TypedQuery<LogicalSpaceGroupMember> query = entityManager.createQuery(
                "SELECT m FROM LogicalSpaceGroupMember m WHERE m.spaceId = :spaceId AND m.deleted = false ORDER BY m.createdAt DESC",
                LogicalSpaceGroupMember.class
        );
        query.setParameter("spaceId", spaceId);
        return query.getResultList();
    }

    @Override
    public List<LogicalSpaceGroupMember> findByTenantId(Long tenantId) {
        TypedQuery<LogicalSpaceGroupMember> query = entityManager.createQuery(
                "SELECT m FROM LogicalSpaceGroupMember m WHERE m.tenantId = :tenantId AND m.deleted = false ORDER BY m.createdAt DESC",
                LogicalSpaceGroupMember.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getResultList();
    }

    @Override
    public long countByGroupId(Long groupId) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(m) FROM LogicalSpaceGroupMember m WHERE m.groupId = :groupId AND m.deleted = false",
                Long.class
        );
        query.setParameter("groupId", groupId);
        return query.getSingleResult();
    }

    @Override
    @Transactional
    public void deleteByGroupId(Long groupId) {
        List<LogicalSpaceGroupMember> members = findByGroupId(groupId);
        members.forEach(this::delete);
        log.debug("Soft deleted all members for groupId: {}", groupId);
    }

    @Override
    @Transactional
    public void deleteBySpaceId(Long spaceId) {
        List<LogicalSpaceGroupMember> members = findBySpaceId(spaceId);
        members.forEach(this::delete);
        log.debug("Soft deleted all group associations for spaceId: {}", spaceId);
    }
}
