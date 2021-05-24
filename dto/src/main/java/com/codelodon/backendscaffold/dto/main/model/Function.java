package com.codelodon.backendscaffold.dto.main.model;

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

@Data
@EqualsAndHashCode(exclude = {"menu", "operation"})
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "fnction")
@NamedEntityGraph(name = "Function.Graph",
        attributeNodes = {@NamedAttributeNode("menu"), @NamedAttributeNode(value = "operation", subgraph = "function.operation")},
        subgraphs = @NamedSubgraph(name = "function.operation", attributeNodes = @NamedAttributeNode("module"))
)
public class Function {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "功能名称不能为空")
    private String name;

    @Column(name = "create_at", nullable = false, updatable = false)
    @CreatedDate
    private Timestamp createAt;

    @Column(name = "modify_at")
    @LastModifiedDate
    private Timestamp modifyAt;

    @JsonIgnoreProperties("functions")
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "menu_id", referencedColumnName = "id")
    @NotNull(message = "菜单不能为空")
    private Menu menu;

    @JsonIgnoreProperties({"functions", "module"})
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "operation_id", referencedColumnName = "id")
    @NotNull(message = "操作不能为空")
    private Operation operation;
}
