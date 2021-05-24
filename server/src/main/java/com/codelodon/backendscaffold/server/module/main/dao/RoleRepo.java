package com.codelodon.backendscaffold.server.module.main.dao;

import com.codelodon.backendscaffold.common.dao.BaseRepo;
import com.codelodon.backendscaffold.dto.main.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface RoleRepo extends BaseRepo<Role, Long> {
    @EntityGraph(value = "Role.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Optional<Role> findById(Long id);

    @EntityGraph(value = "Role.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Page<Role> findAll(@Nullable Specification<Role> specification, Pageable pageable);

    @Modifying
    @Query("UPDATE Role set " +
            "name = :#{#r.name} " +
            "WHERE id = :#{#r.id}")
    void updateInfo(@Param("r") Role role);
}
