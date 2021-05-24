package com.codelodon.backendscaffold.dto.main.model;

import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "file")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "文件记录名不能为空")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "原文件名不能为空")
    private String filename;

    @Column(nullable = false)
    @NotNull(message = "文件大小不能为空")
    @Min(value = 0, message = "文件大小不能为负数")
    private Long size;

    @Column(name = "file_type", nullable = false)
    @NotBlank(message = "文件类型不能为空")
    private String fileType;
}
