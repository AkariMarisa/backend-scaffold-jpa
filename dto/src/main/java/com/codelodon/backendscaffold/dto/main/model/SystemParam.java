package com.codelodon.backendscaffold.dto.main.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "system_param")
public class SystemParam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ky", nullable = false, unique = true)
    @NotBlank(message = "Key 不能为空")
    private String key;

    @Column(name = "val")
    private String value;

    @Column(name = "create_at", nullable = false, updatable = false)
    @CreatedDate
    private Timestamp createAt;

    @Column(name = "modify_at")
    @LastModifiedDate
    private Timestamp modifyAt;
}
