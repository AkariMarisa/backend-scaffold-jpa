package com.codelodon.backendscaffold.dto.main.model;

import com.codelodon.backendscaffold.common.entity.*;
import com.codelodon.backendscaffold.dto.main.entity.UserType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Set;

/**
 * 用户表
 */
@Data
@EqualsAndHashCode(exclude = {"role", "functions", "userOperations", "userFunctions"})
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user")
@SQLDelete(sql = "UPDATE user SET is_deleted = true where id = ?")
@Where(clause = "is_deleted = false")
@NamedEntityGraph(name = "User.Graph", attributeNodes = {@NamedAttributeNode("role"), @NamedAttributeNode("userOperations"), @NamedAttributeNode("userFunctions")})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "用户名不能为空", groups = {CreateGroup.class, ModifyGroup.class, AuthGroup.class})
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    @NotBlank(message = "密码不能为空", groups = {CreateGroup.class, AuthGroup.class, ChangePasswordGroup.class})
    @Convert(converter = PasswordConverter.class)
    private String password;

    @Column(nullable = false)
    @NotNull(message = "用户类型不能为空", groups = {CreateGroup.class, ModifyGroup.class})
    @Enumerated(EnumType.STRING)
    private UserType type;

    @Column(nullable = false)
    @NotBlank(message = "姓名不能为空", groups = {CreateGroup.class, ModifyGroup.class})
    private String realName;

    @Column(nullable = false)
    @NotBlank(message = "职位不能为空", groups = {CreateGroup.class, ModifyGroup.class})
    private String post;

    @Column
    private String tel;

    @Column(name = "is_enabled", nullable = false)
    @NotNull(message = "启用状态不可为空", groups = {UserUsableGroup.class})
    private Boolean isEnabled = true;

    @Column(name = "create_at", nullable = false, updatable = false)
    @CreatedDate
    private Timestamp createAt;

    @Column(name = "modify_at")
    @LastModifiedDate
    private Timestamp modifyAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @JsonIgnoreProperties({"users", "operations"})
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    @NotNull(message = "用户角色不能为空", groups = {CreateGroup.class, ModifyGroup.class})
    private Role role;

    @Transient
    private Set<Function> functions;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_operations_id", referencedColumnName = "id")
    private UserOperations userOperations;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_functions_id", referencedColumnName = "id")
    private UserFunctions userFunctions;
}

/**
 * 用户密码转换器，用于在保存时对密码加密
 */
@Converter
class PasswordConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String s) {
        return new BCryptPasswordEncoder().encode(s);
    }

    @Override
    public String convertToEntityAttribute(String s) {
        return s;
    }
}
