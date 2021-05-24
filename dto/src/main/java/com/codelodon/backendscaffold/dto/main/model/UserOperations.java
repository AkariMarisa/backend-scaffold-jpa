package com.codelodon.backendscaffold.dto.main.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(exclude = "user")
@Entity
@Table(name = "user_operations")
@NamedEntityGraph(name = "UserOperations.Graph", attributeNodes = {
        @NamedAttributeNode("user")
})
public class UserOperations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "text")
    private String operationIds;

    @JsonIgnoreProperties("userOperations")
    @OneToOne(mappedBy = "userOperations", cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private User user;

}
