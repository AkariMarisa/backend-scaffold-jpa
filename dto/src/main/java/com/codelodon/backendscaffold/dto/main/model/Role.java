package com.codelodon.backendscaffold.dto.main.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;
import java.util.Set;

/**
 * 角色表
 */
@Data
@EqualsAndHashCode(exclude = {"users", "functions", "roleOperations", "roleFunctions"})
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "role")
@SQLDelete(sql = "UPDATE role SET is_deleted = true where id = ?")
@Where(clause = "is_deleted = false")
@NamedEntityGraph(name = "Role.Graph", attributeNodes = {@NamedAttributeNode("roleOperations"), @NamedAttributeNode("roleFunctions")})
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "角色名称不能为空")
    private String name;

    @Column(name = "create_at", nullable = false, updatable = false)
    @CreatedDate
    private Timestamp createAt;

    @Column(name = "modify_at")
    @LastModifiedDate
    private Timestamp modifyAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Transient
    private Set<Function> functions;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_operations_id", referencedColumnName = "id")
    private RoleOperations roleOperations;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_functions_id", referencedColumnName = "id")
    private RoleFunctions roleFunctions;
}
