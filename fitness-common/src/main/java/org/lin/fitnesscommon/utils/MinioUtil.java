package org.lin.fitnesscommon.utils;

/**
 * @author lin
 * @date 2026-03-25
 */
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.lin.fitnesscommon.config.MinioProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class MinioUtil {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioProperties minioProperties;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * 检查桶是否存在，不存在则创建
     */
    public void checkAndCreateBucket(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("创建桶：{}", bucketName);
        }
    }

    /**
     * 上传文件（通用方法）
     * @param inputStream 文件输入流
     * @param fileName 文件名（包含扩展名）
     * @param contentType 文件类型
     * @param bucketName 桶名称
     * @return 文件访问 URL
     */
    public String uploadFile(InputStream inputStream, String fileName,
                            String contentType, String bucketName) throws Exception {
        checkAndCreateBucket(bucketName);

        String objectName = generateObjectName(fileName);

        // 根据文件类型设置不同的大小限制
        long maxSize;
        if (contentType != null && contentType.startsWith("video/")) {
            maxSize = 100 * 1024 * 1024; // 视频最大 100MB
        } else {
            maxSize = 10 * 1024 * 1024;  // 其他文件最大 10MB
        }

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(inputStream, -1, maxSize)
                .contentType(contentType)
                .build());

        log.info("文件上传成功：{}/{}", bucketName, objectName);
        return getObjectUrl(bucketName, objectName);
    }

    /**
     * 从 MultipartFile 上传文件
     */
    public String uploadMultipartFile(MultipartFile file, String bucketName) throws Exception {
        checkAndCreateBucket(bucketName);

        // 检查文件大小
        long fileSize = file.getSize();
        String contentType = file.getContentType();
        
        if (contentType != null && contentType.startsWith("video/")) {
            // 视频文件最大 100MB
            if (fileSize > 100 * 1024 * 1024) {
                throw new IllegalArgumentException("视频文件过大，不能超过 100MB，当前大小：" + formatFileSize(fileSize));
            }
        } else {
            // 其他文件最大 10MB
            if (fileSize > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("文件过大，不能超过 10MB，当前大小：" + formatFileSize(fileSize));
            }
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = generateFileName(extension);

        try (InputStream inputStream = file.getInputStream()) {
            return uploadFile(inputStream, fileName, file.getContentType(), bucketName);
        }
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 上传图片
     */
    public String uploadImage(MultipartFile file) throws Exception {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("不支持的图片格式");
        }
        return uploadMultipartFile(file, minioProperties.getImageBucketName());
    }

    /**
     * 上传视频
     */
    public String uploadVideo(MultipartFile file) throws Exception {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("不支持的视频格式");
        }
        return uploadMultipartFile(file, minioProperties.getVideoBucketName());
    }

    /**
     * 删除文件
     */
    public void deleteFile(String bucketName, String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
        log.info("文件删除成功：{}/{}", bucketName, objectName);
    }

    /**
     * 获取文件访问 URL（带签名，有效期 7 天）
     */
    public String getObjectUrl(String bucketName, String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(604800)
                .build());
    }

    /**
     * 生成对象名称（按日期分类）
     */
    private String generateObjectName(String fileName) {
        String datePath = LocalDateTime.now().format(DATE_FORMATTER);
        return datePath + "/" + fileName;
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex).toLowerCase();
    }

    /**
     * 列出桶中的所有对象
     */
    public List<String> listObjects(String bucketName) throws Exception {
        List<String> objectNames = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).recursive(true).build());
        for (Result<Item> result : results) {
            objectNames.add(result.get().objectName());
        }
        return objectNames;
    }

    /**
     * 检查文件是否存在
     */
    public boolean isObjectExist(String bucketName, String objectName) throws Exception {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
