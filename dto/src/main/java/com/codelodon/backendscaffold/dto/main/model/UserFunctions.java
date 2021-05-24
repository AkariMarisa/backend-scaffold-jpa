package com.codelodon.backendscaffold.dto.main.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(exclude = "user")
@Entity
@Table(name = "user_functions")
@NamedEntityGraph(name = "UserFunctions.Graph", attributeNodes = {
        @NamedAttributeNode("user")
})
public class UserFunctions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "text")
    private String functionIds;

    @JsonIgnoreProperties("userFunctions")
    @OneToOne(mappedBy = "userFunctions", cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private User user;

}
