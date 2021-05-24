package com.codelodon.backendscaffold.dto.main.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = "functions")
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "menu")
@NamedEntityGraph(name = "Menu.Graph",
        attributeNodes = {@NamedAttributeNode(value = "functions", subgraph = "menu.functions")},
        subgraphs = @NamedSubgraph(name = "menu.functions", attributeNodes = @NamedAttributeNode("operation"))
)
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "菜单名称不能为空")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "菜单URL不能为空")
    private String url;

    @Column(name = "create_at", nullable = false, updatable = false)
    @CreatedDate
    private Timestamp createAt;

    @Column(name = "modify_at")
    @LastModifiedDate
    private Timestamp modifyAt;

    @JsonIgnoreProperties("menu")
    @OneToMany(mappedBy = "menu", cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @OrderBy("id desc")
    private Set<Function> functions;
}
