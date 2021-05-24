package com.codelodon.backendscaffold.server.module.main.dao;

import com.codelodon.backendscaffold.common.dao.BaseRepo;
import com.codelodon.backendscaffold.dto.main.model.RoleFunctions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface RoleFunctionsRepo extends BaseRepo<RoleFunctions, Long> {
    @EntityGraph(value = "RoleFunctions.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Optional<RoleFunctions> findById(Long id);

    @EntityGraph(value = "RoleFunctions.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Page<RoleFunctions> findAll(@Nullable Specification<RoleFunctions> specification, Pageable pageable);
}
