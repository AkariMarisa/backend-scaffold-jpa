package com.codelodon.backendscaffold.dto.main.model;

import com.codelodon.backendscaffold.dto.main.entity.HttpMethod;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

/**
 * 模块操作项表
 * 每个模块会有自己的操作项，比如库存管理的新增、修改、查询等
 */
@Data
@EqualsAndHashCode(exclude = {"module"})
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "operation")
@NamedEntityGraph(
        name = "Operation.Graph",
        attributeNodes = {@NamedAttributeNode(value = "module", subgraph = "operation.module")},
        subgraphs = @NamedSubgraph(name = "operation.module", attributeNodes = @NamedAttributeNode("subSystem"))
)
public class Operation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "操作类型不能为空")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "接口类名不能为空")
    private String fullClassName;

    @Column(nullable = false)
    @NotBlank(message = "接口方法名不能为空")
    private String methodName;

    @Column(nullable = false)
    @NotNull(message = "接口请求方式不能为空")
    @Enumerated(EnumType.STRING)
    private HttpMethod type;

    @Column(name = "need_auth", nullable = false)
    @NotNull(message = "接口是否需要鉴权不能为空")
    private Boolean needAuth;

    @Column(name = "create_at", nullable = false, updatable = false)
    @CreatedDate
    private Timestamp createAt;

    @Column(name = "modify_at")
    @LastModifiedDate
    private Timestamp modifyAt;

    @JsonIgnoreProperties("operations")
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "module_id", referencedColumnName = "id")
    @NotNull(message = "模块不能为空")
    private Module module;
}
