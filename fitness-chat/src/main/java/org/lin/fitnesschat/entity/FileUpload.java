package org.lin.fitnesschat.entity;

/**
 * @author lin
 * @date 2026-04-17
 */

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 文件上传实体类
 * 用于表示文件上传的相关信息
 * 数据库主键为 file_md5，无自增 id 列
 */
@Data
@Entity
@Table(name = "file_upload")
public class FileUpload {

    /**
     * 文件的唯一标识符（主键）
     * 直接使用文件的 MD5 值作为主键，与数据库表结构一致
     */
    @Id
    @Column(name = "file_md5", length = 32, nullable = false)
    private String fileMd5;

    /**
     * 文件的原始名称
     * 用于记录上传时文件的名称
     */
    @Column(name = "file_name", nullable = false)
    private String fileName;

    /**
     * 文件的总大小
     * 以字节为单位记录文件的大小
     */
    @Column(name = "total_size", nullable = false)
    private long totalSize;

    /**
     * 文件上传的状态
     * 0表示文件正在上传中，1表示文件上传已完成
     */
    @Column(name = "status", nullable = false)
    private int status; // 0-上传中 1-已完成

    /**
     * 上传文件的用户的标识符
     * 用于记录哪个用户上传了文件
     */
    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    /**
     * 文件是否公开
     * true表示所有用户可访问，false表示仅上传者可访问
     */
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    /**
     * 文件上传的创建时间
     * 自动记录文件上传开始的时间
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 文件合并完成的时间
     * 当文件上传状态为已完成时，自动记录完成的时间
     */
    @UpdateTimestamp
    @Column(name = "merged_at")
    private LocalDateTime mergedAt;
}
