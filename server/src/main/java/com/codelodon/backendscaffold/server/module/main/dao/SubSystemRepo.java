package com.codelodon.backendscaffold.server.module.main.dao;

import com.codelodon.backendscaffold.common.dao.BaseRepo;
import com.codelodon.backendscaffold.dto.main.model.SubSystem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface SubSystemRepo extends BaseRepo<SubSystem, Long> {
    @EntityGraph(value = "SubSystem.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Optional<SubSystem> findById(Long id);

    @EntityGraph(value = "SubSystem.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Page<SubSystem> findAll(@Nullable Specification<SubSystem> specification, Pageable pageable);

    @EntityGraph(value = "SubSystem.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT ss FROM SubSystem ss WHERE ss.name = :name AND ss.packageName = :packageName")
    Optional<SubSystem> findByNameAndPackageName(@Param("name") final String name, @Param("packageName") final String packageName);
}
