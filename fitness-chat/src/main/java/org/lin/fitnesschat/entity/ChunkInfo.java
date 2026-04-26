package org.lin.fitnesschat.entity;

/**
 * @author lin
 * @date 2026-04-17
 */

import jakarta.persistence.*;
import lombok.Data;

/**
 * ChunkInfo 类用于表示文件分块的信息
 * 它是一个实体类，与数据库中的 'chunk_info' 表对应
 * 该类用来存储每个文件分块的元数据，包括分块的唯一标识、属于哪个文件、分块的顺序、分块的校验码和存储位置
 */
@Data
@Entity
@Table(name = "chunk_info",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_file_md5_chunk_index",
                columnNames = {"file_md5", "chunk_index"}
        ))
public class ChunkInfo {

    /**
     * 物理主键：分块信息的唯一标识符
     * 由数据库自动生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 逻辑外键：文件的MD5值
     * 对应 FileUpload 的主键 file_md5，标识该分片属于哪个文件
     */
    @Column(name = "file_md5", length = 32, nullable = false)
    private String fileMd5;

    /**
     * 分块的索引号
     * 表示文件中的第几个分块，用于保持分块的顺序
     */
    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    /**
     * 分块的MD5值
     * 每个分块的唯一标识，用于校验分块的完整性和正确性
     */
    @Column(name = "chunk_md5", length = 32)
    private String chunkMd5;

    /**
     * 分块的存储路径
     * 表示分块在系统中的存储位置
     */
    @Column(name = "storage_path", length = 512)
    private String storagePath;
}

