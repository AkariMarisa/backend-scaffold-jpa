package com.codelodon.backendscaffold.dto.main.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(exclude = "role")
@Entity
@Table(name = "role_functions")
@NamedEntityGraph(name = "RoleFunctions.Graph", attributeNodes = {
        @NamedAttributeNode("role")
})
public class RoleFunctions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "text")
    private String functionIds;

    @JsonIgnoreProperties("roleFunctions")
    @OneToOne(mappedBy = "roleFunctions", cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private Role role;

}
