package org.lin.fitnesscommon.config;

/**
 * @author lin
 * @date 2026-03-25
 */

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 用于获取 Minio 配置信息
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;// Minio 端点
    private String accessKey;// Minio 访问密钥
    private String secretKey;// Minio 密钥
    private String bucketName;// Minio 默认存储桶名称
    private String imageBucketName;// Minio 图片存储桶名称
    private String videoBucketName;// Minio 视频存储桶名称
}
