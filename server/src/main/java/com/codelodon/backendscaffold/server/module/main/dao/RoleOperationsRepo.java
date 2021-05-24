package com.codelodon.backendscaffold.server.module.main.dao;

import com.codelodon.backendscaffold.common.dao.BaseRepo;
import com.codelodon.backendscaffold.dto.main.model.RoleOperations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface RoleOperationsRepo extends BaseRepo<RoleOperations, Long> {
    @EntityGraph(value = "RoleOperations.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Optional<RoleOperations> findById(Long id);

    @EntityGraph(value = "RoleOperations.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Page<RoleOperations> findAll(@Nullable Specification<RoleOperations> specification, Pageable pageable);
}
