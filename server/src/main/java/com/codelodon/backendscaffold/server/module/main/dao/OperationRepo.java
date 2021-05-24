package com.codelodon.backendscaffold.server.module.main.dao;

import com.codelodon.backendscaffold.common.dao.BaseRepo;
import com.codelodon.backendscaffold.dto.main.entity.HttpMethod;
import com.codelodon.backendscaffold.dto.main.model.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface OperationRepo extends BaseRepo<Operation, Long> {
    @EntityGraph(value = "Operation.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Optional<Operation> findById(Long id);

    @EntityGraph(value = "Operation.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Override
    Page<Operation> findAll(@Nullable Specification<Operation> specification, Pageable pageable);

    @EntityGraph(value = "Operation.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT o FROM Operation o WHERE o.fullClassName = :fullClassName AND o.methodName = :methodName AND o.type = :requestMethod")
    List<Operation> findForAuth(@Param("requestMethod") HttpMethod requestMethod, @Param("fullClassName") String fullClassName, @Param("methodName") String methodName);

    @EntityGraph(value = "Operation.Graph", type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT o FROM Operation o WHERE o.fullClassName = :fullClassname AND o.name = :name AND o.methodName = :methodName AND o.type = :requestMethod")
    Optional<Operation> findHuh(
            @Param("fullClassname") final String fullClassname,
            @Param("name") final String name,
            @Param("methodName") final String methodName,
            @Param("requestMethod") final HttpMethod requestMethod);

    @Modifying
    @Query("DELETE FROM Operation WHERE id = :id")
    void deleteById(@Param("id") Long id);
}
