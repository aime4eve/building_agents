package com.hkt.iot.user.infrastructure.persistence;

import com.hkt.iot.user.domain.model.User;
import com.hkt.iot.user.domain.model.User.UserStatus;
import com.hkt.iot.user.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户Repository实现
 * 基于DDL: user表
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
public class UserRepositoryImpl implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
            log.debug("Created new user: id={}, username={}", user.getId(), user.getUsername());
            return user;
        } else {
            User merged = entityManager.merge(user);
            log.debug("Updated user: id={}, username={}", merged.getId(), merged.getUsername());
            return merged;
        }
    }

    @Override
    public List<User> saveAll(List<User> entities) {
        return entities.stream()
                .map(this::save)
                .toList();
    }

    @Override
    public Optional<User> findById(Long id) {
        User user = entityManager.find(User.class, id);
        if (user != null && !user.getDeleted()) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.id IN :ids AND u.deleted = false",
            User.class
        );
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    @Override
    public List<User> findAll() {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.deleted = false ORDER BY u.createdAt DESC",
            User.class
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
            "SELECT COUNT(u) FROM User u WHERE u.deleted = false",
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
    public void delete(User user) {
        user.setDeleted(true);
        user.setDeletedAt(java.time.LocalDateTime.now());
        entityManager.merge(user);
        log.debug("Soft deleted user: id={}", user.getId());
    }

    @Override
    public void deleteAll(List<User> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        findAll().forEach(this::delete);
    }

    @Override
    public Optional<User> findByTenantIdAndUsername(Long tenantId, String username) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.username = :username AND u.deleted = false",
            User.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("username", username);

        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.email = :email AND u.deleted = false",
            User.class
        );
        query.setParameter("email", email);

        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.phone = :phone AND u.deleted = false",
            User.class
        );
        query.setParameter("phone", phone);

        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findByTenantId(Long tenantId) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.deleted = false ORDER BY u.createdAt DESC",
            User.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getResultList();
    }

    @Override
    public List<User> findByUserStatus(UserStatus userStatus) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.userStatus = :userStatus AND u.deleted = false ORDER BY u.createdAt DESC",
            User.class
        );
        query.setParameter("userStatus", userStatus);
        return query.getResultList();
    }

    @Override
    public List<User> findByTenantIdAndUserStatus(Long tenantId, UserStatus userStatus) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.userStatus = :userStatus AND u.deleted = false ORDER BY u.createdAt DESC",
            User.class
        );
        query.setParameter("tenantId", tenantId);
        query.setParameter("userStatus", userStatus);
        return query.getResultList();
    }

    @Override
    public boolean existsByTenantIdAndUsername(Long tenantId, String username) {
        return findByTenantIdAndUsername(tenantId, username).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public long countByTenantId(Long tenantId) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.deleted = false",
            Long.class
        );
        query.setParameter("tenantId", tenantId);
        return query.getSingleResult();
    }

    @Override
    public List<User> findByTenantIdPaging(Long tenantId, int page, int size) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.deleted = false ORDER BY u.createdAt DESC",
            User.class
        );
        query.setParameter("tenantId", tenantId);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }
}
