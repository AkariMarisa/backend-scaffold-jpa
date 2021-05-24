package com.codelodon.backendscaffold.server.module.main.dao;

import com.codelodon.backendscaffold.common.dao.BaseRepo;
import com.codelodon.backendscaffold.dto.main.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface UserRepo extends BaseRepo<User, Long> {
    @EntityGraph(value = "User.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Optional<User> findById(Long id);

    @EntityGraph(value = "User.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Page<User> findAll(@Nullable Specification<User> specification, Pageable pageable);

    @EntityGraph(value = "User.Graph", type = EntityGraph.EntityGraphType.FETCH)
    Optional<User> findByUsername(String username);

    @Modifying
    @Query("UPDATE User set " +
            "username = :#{#u.username}, " +
            "role = :#{#u.role}, " +
            "realName = :#{#u.realName}, " +
            "post = :#{#u.post}, " +
            "tel = :#{#u.tel} " +
            "WHERE id = :#{#u.id}")
    void updateInfo(@Param("u") User user);

    @Modifying
    @Query("UPDATE User set password = :#{#password} where id = :#{#id}")
    void updatePassword(@Param("id") Long id, @Param("password") String password);

    @Modifying
    @Query("UPDATE User set isEnabled = :#{#isEnabled} where id = :#{#id}")
    void updateUsability(@Param("id") Long id, @Param("isEnabled") Boolean isEnabled);

    @Modifying
    @Query("DELETE FROM User WHERE id = :id")
    void deleteById(@Param("id") final Long id);
}
