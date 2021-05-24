package com.codelodon.backendscaffold.dto.main.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(exclude = "role")
@Entity
@Table(name = "role_operations")
@NamedEntityGraph(name = "RoleOperations.Graph", attributeNodes = {
        @NamedAttributeNode("role")
})
public class RoleOperations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "text")
    private String operationIds;

    @JsonIgnoreProperties("roleOperations")
    @OneToOne(mappedBy = "roleOperations", cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private Role role;

}
