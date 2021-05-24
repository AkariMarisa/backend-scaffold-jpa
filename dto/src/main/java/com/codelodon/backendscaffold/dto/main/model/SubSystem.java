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

/**
 * 子系统表
 * 在系统里，每个模块作为一个子系统表示。
 * 这里的模块代指 仓库管理、财务管理
 */
@Data
@EqualsAndHashCode(exclude = {"modules"})
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "subsystem")
@NamedEntityGraph(
        name = "SubSystem.Graph",
        attributeNodes = {@NamedAttributeNode(value = "modules", subgraph = "subsystem.modules")},
        subgraphs = @NamedSubgraph(name = "subsystem.modules", attributeNodes = @NamedAttributeNode("operations"))
)
public class SubSystem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "系统名称不能为空")
    private String name;

    @Column(name = "package_name", nullable = false)
    @NotBlank(message = "系统基础包名不能为空")
    private String packageName;

    @Column(name = "create_at", nullable = false, updatable = false)
    @CreatedDate
    private Timestamp createAt;

    @Column(name = "modify_at")
    @LastModifiedDate
    private Timestamp modifyAt;

    @JsonIgnoreProperties("subSystem")
    @OneToMany(mappedBy = "subSystem", cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @OrderBy("id desc")
    private Set<Module> modules;
}
