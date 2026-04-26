package org.lin.fitnesschat.entity;

/**
 * @author lin
 * @date 2026-04-17
 */

import jakarta.persistence.*;
import lombok.Data;

/**
 * 文档向量实体类
 * 用于存储文本分块和相关元数据
 */
@Data
@Entity
@Table(name = "document_vectors",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_file_md5_chunk_id",
                columnNames = {"file_md5", "chunk_id"}
        ))
public class DocumentVector {

    /**
     * 物理主键：向量记录的唯一标识符
     * 由数据库自动生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vectorId;

    /**
     * 逻辑外键：文件的MD5值
     * 对应 FileUpload 的主键 file_md5，标识该向量属于哪个文件
     */
    @Column(name = "file_md5", nullable = false, length = 32)
    private String fileMd5;

    /**
     * 文本分块序号
     * 与 file_md5 组成联合唯一约束，确保一个文件的每个分块只有一条向量记录
     */
    @Column(name = "chunk_id", nullable = false)
    private Integer chunkId;

    @Lob
    @Column(name = "text_content")
    private String textContent;

    @Column(name = "model_version", length = 32)
    private String modelVersion;

    /**
     * 上传用户ID
     */
    @Column(nullable = false, name = "user_id", length = 64)
    private String userId;

    /**
     * 文件是否公开
     */
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;
}
