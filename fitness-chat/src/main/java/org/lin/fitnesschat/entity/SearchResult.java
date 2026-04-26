package org.lin.fitnesschat.entity;

import lombok.Data;

/**
 * @author lin
 * @date 2026-04-17
 */

@Data
public class SearchResult {
    private String fileMd5;    // 文件指纹
    private Integer chunkId;   // 文本分块序号
    private String textContent; // 文本内容
    private Double score;      // 搜索得分
    private String fileName;   // 原始文件名
    private String userId;     // 上传用户ID
    private Boolean isPublic;  // 是否公开

    public SearchResult(String fileMd5, Integer chunkId, String textContent, Double score) {
        this(fileMd5, chunkId, textContent, score, null, false, null);
    }

    public SearchResult(String fileMd5, Integer chunkId, String textContent, Double score, String fileName) {
        this(fileMd5, chunkId, textContent, score, null, false, fileName);
    }

    public SearchResult(String fileMd5, Integer chunkId, String textContent, Double score, String userId, boolean isPublic) {
        this(fileMd5, chunkId, textContent, score, userId,  isPublic, null);
    }

    public SearchResult(String fileMd5, Integer chunkId, String textContent, Double score, String userId, boolean isPublic, String fileName) {
        this.fileMd5 = fileMd5;
        this.chunkId = chunkId;
        this.textContent = textContent;
        this.score = score;
        this.userId = userId;
        this.isPublic = isPublic;
        this.fileName = fileName;
    }
}
