package com.codelodon.backendscaffold.server.module.main.dao;

import com.codelodon.backendscaffold.common.dao.BaseRepo;
import com.codelodon.backendscaffold.dto.main.model.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface FunctionRepo extends BaseRepo<Function, Long> {
    @EntityGraph(value = "Function.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Iterable<Function> findAllById(Iterable<Long> iterable);

    @EntityGraph(value = "Function.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Optional<Function> findById(Long id);

    @EntityGraph(value = "Function.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Page<Function> findAll(@Nullable Specification<Function> specification, Pageable pageable);

    @Modifying
    @Query(nativeQuery = true, value = "DELETE f FROM fnction f INNER JOIN menu m ON m.id = f.menu_id WHERE m.id = :menuId")
    void deleteByMenu(@Param("menuId") Long menuId);

    @Modifying
    @Query(nativeQuery = true, value = "DELETE f FROM fnction f INNER JOIN operation o ON o.id = f.operation_id WHERE o.id = :operationId")
    void deleteByOperation(@Param("operationId") Long operationId);
}
