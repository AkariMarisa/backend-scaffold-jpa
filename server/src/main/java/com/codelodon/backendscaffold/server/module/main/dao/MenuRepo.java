package com.codelodon.backendscaffold.server.module.main.dao;

import com.codelodon.backendscaffold.common.dao.BaseRepo;
import com.codelodon.backendscaffold.dto.main.model.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface MenuRepo extends BaseRepo<Menu, Long> {
    @EntityGraph(value = "Menu.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Optional<Menu> findById(Long id);

    @EntityGraph(value = "Menu.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Page<Menu> findAll(@Nullable Specification<Menu> specification, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Menu WHERE id = :id")
    void deleteById(@Param("id") Long id);
}
