package com.codelodon.backendscaffold.server.module.main.dao;

import com.codelodon.backendscaffold.common.dao.BaseRepo;
import com.codelodon.backendscaffold.dto.main.model.UserFunctions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface UserFunctionsRepo extends BaseRepo<UserFunctions, Long> {
    @EntityGraph(value = "UserFunctions.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Optional<UserFunctions> findById(Long id);

    @EntityGraph(value = "UserFunctions.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Page<UserFunctions> findAll(@Nullable Specification<UserFunctions> specification, Pageable pageable);

    @Modifying
    @Query("DELETE FROM UserFunctions WHERE id = :id")
    void deleteById(@Param("id") final Long id);
}
