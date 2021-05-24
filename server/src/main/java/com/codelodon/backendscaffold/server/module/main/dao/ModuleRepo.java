package com.codelodon.backendscaffold.server.module.main.dao;

import com.codelodon.backendscaffold.common.dao.BaseRepo;
import com.codelodon.backendscaffold.dto.main.model.Module;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface ModuleRepo extends BaseRepo<Module, Long> {
    @EntityGraph(value = "Module.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Optional<Module> findById(Long id);

    @EntityGraph(value = "Module.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Page<Module> findAll(@Nullable Specification<Module> specification, Pageable pageable);

    @EntityGraph(value = "Module.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT m FROM Module m WHERE m.name = :name AND m.fullClassname = :fullClassname")
    Optional<Module> findByNameAndFullClassname(@Param("name") final String name, @Param("fullClassname") final String fullClassname);
}
