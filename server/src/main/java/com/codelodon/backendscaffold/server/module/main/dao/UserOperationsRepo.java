package com.codelodon.backendscaffold.server.module.main.dao;

import com.codelodon.backendscaffold.common.dao.BaseRepo;
import com.codelodon.backendscaffold.dto.main.model.UserOperations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface UserOperationsRepo extends BaseRepo<UserOperations, Long> {
    @EntityGraph(value = "UserOperations.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Optional<UserOperations> findById(Long id);

    @EntityGraph(value = "UserOperations.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Page<UserOperations> findAll(@Nullable Specification<UserOperations> specification, Pageable pageable);

    @Modifying
    @Query("DELETE FROM UserOperations WHERE id = :id")
    void deleteById(@Param("id") final Long id);
}
