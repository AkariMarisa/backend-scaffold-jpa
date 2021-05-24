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
import java.util.Set;

/**
 * 模块表
 * 子系统分为不同模块，比如仓库管理的库存管理
 */
@Data
@EqualsAndHashCode(exclude = {"subSystem", "operations"})
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "module")
@NamedEntityGraph(name = "Module.Graph", attributeNodes = {@NamedAttributeNode("subSystem"), @NamedAttributeNode("operations")})
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "模块名称不能为空")
    private String name;

    @Column(name = "full_classname", nullable = false)
    @NotBlank(message = "模块完整类名不能为空")
    private String fullClassname;

    @Column(name = "create_at", nullable = false, updatable = false)
    @CreatedDate
    private Timestamp createAt;

    @Column(name = "modify_at")
    @LastModifiedDate
    private Timestamp modifyAt;

    @JsonIgnoreProperties("modules")
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "subsystem_id", referencedColumnName = "id")
    @NotNull(message = "子系统不能为空")
    private SubSystem subSystem;

    @JsonIgnoreProperties("module")
    @OneToMany(mappedBy = "module", cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @OrderBy("id desc")
    private Set<Operation> operations;

    @Override
    public String toString() {
        return "Module{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", fullClassname='" + fullClassname + '\'' +
                ", createAt=" + createAt +
                ", modifyAt=" + modifyAt +
                ", subSystem=(id=" + subSystem.getId() + ",name='" + subSystem.getName() + '\'' + ")" +
                '}';
    }
}
