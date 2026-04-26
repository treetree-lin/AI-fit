package org.lin.fitnesschat.entity;
import lombok.Data;
/**
 * @author lin
 * @date 2026-04-17
 */



/**
 * Elasticsearch存储的文档实体类
 * 包含文档内容和权限信息
 */
@Data
public class EsDocument {

    /**
     * ES 文档唯一标识（逻辑主键）
     * 生成规则：fileMd5 + "_" + chunkId，确保同一文件分块幂等索引
     */
    private String id;

    /**
     * 文件指纹（逻辑外键）
     * 对应 FileUpload 的主键 file_md5
     */
    private String fileMd5;

    /**
     * 文本分块序号
     */
    private Integer chunkId;
    private String textContent;    // 文本内容
    private float[] vector;        // 向量数据（768维）
    private String modelVersion;   // 向量生成模型版本
    private String userId;         // 上传用户ID
    private boolean isPublic;      // 是否公开

    /**
     * 默认构造函数，用于Jackson反序列化
     */
    public EsDocument() {
    }

    /**
     * 完整构造函数，包含权限字段
     */
    public EsDocument(String id, String fileMd5, int chunkId, String content,
                      float[] vector, String modelVersion,
                      String userId, boolean isPublic) {
        this.id = id;
        this.fileMd5 = fileMd5;
        this.chunkId = chunkId;
        this.textContent = content;
        this.vector = vector;
        this.modelVersion = modelVersion;
        this.userId = userId;
        this.isPublic = isPublic;
    }


}
