package org.lin.fitnesschat.controller;

import org.lin.fitnesschat.config.KafkaConfig;
import org.lin.fitnesschat.entity.FileProcessingTask;
import org.lin.fitnesschat.entity.FileUpload;
import org.lin.fitnesschat.repository.FileUploadRepository;
import org.lin.fitnesschat.service.FileTypeValidationService;
import org.lin.fitnesschat.service.UploadService;
import org.lin.fitnessuser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * @author lin
 * @date 2026-04-16
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {
    @Autowired
    private UploadService uploadService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private UserService userService;

    @Autowired
    private FileUploadRepository fileUploadRepository;

    @Autowired
    private FileTypeValidationService fileTypeValidationService;

    public UploadController(UploadService uploadService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.uploadService = uploadService;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 上传文件分片接口
     *
     * @param fileMd5 文件的MD5值，用于唯一标识文件
     * @param chunkIndex 分片索引，表示当前分片的位置
     * @param totalSize 文件总大小
     * @param fileName 文件名
     * @param totalChunks 总分片数量
     * @param isPublic 是否公开，默认为false
     * @param file 分片文件对象
     * @return 返回包含已上传分片和上传进度的响应
     * @throws IOException 当文件读写发生错误时抛出
     */
    @PostMapping("/chunk")
    public ResponseEntity<Map<String, Object>> uploadChunk(
            @RequestParam("fileMd5") String fileMd5,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalSize") long totalSize,
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "totalChunks", required = false) Integer totalChunks,
            @RequestParam(value = "isPublic", required = false, defaultValue = "false") boolean isPublic,
            @RequestParam("file") MultipartFile file,
            @RequestAttribute("userId") String userId) throws IOException {

        try {
            // 文件类型验证（仅在第一个分片时进行验证）
            if (chunkIndex == 0) {
                FileTypeValidationService.FileTypeValidationResult validationResult =
                        fileTypeValidationService.validateFileType(fileName);


                if (!validationResult.isValid()) {

                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("code", HttpStatus.BAD_REQUEST.value());
                    errorResponse.put("message", validationResult.getMessage());
                    errorResponse.put("fileType", validationResult.getFileType());
                    errorResponse.put("supportedTypes", fileTypeValidationService.getSupportedFileTypes());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
            }

            String fileType = getFileType(fileName);
            String contentType = file.getContentType();






            uploadService.uploadChunk(fileMd5, chunkIndex, totalSize, fileName, file, isPublic, userId);

            List<Integer> uploadedChunks = uploadService.getUploadedChunks(fileMd5, userId);
            int actualTotalChunks = uploadService.getTotalChunks(fileMd5, userId);
            double progress = calculateProgress(uploadedChunks, actualTotalChunks);



            // 构建数据对象
            Map<String, Object> data = new HashMap<>();
            data.put("uploaded", uploadedChunks);
            data.put("progress", progress);

            // 构建统一响应格式
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "分片上传成功");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            String fileType = getFileType(fileName);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("message", "分片上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取文件上传状态接口
     *
     * @param fileMd5 文件的MD5值，用于唯一标识文件
     * @return 返回包含已上传分片和上传进度的响应
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getUploadStatus(@RequestParam("file_md5") String fileMd5, @RequestAttribute("userId") String userId) {
        try {
            // 获取文件信息
            String fileName = "unknown";
            String fileType = "unknown";
            try {
                Optional<FileUpload> fileUpload = fileUploadRepository.findByFileMd5(fileMd5);
                if (fileUpload.isPresent()) {
                    fileName = fileUpload.get().getFileName();
                    fileType = getFileType(fileName);
                }
            } catch (Exception e) {
                // 获取文件信息失败不影响状态查询，继续处理
            }


            List<Integer> uploadedChunks = uploadService.getUploadedChunks(fileMd5, userId);
            int totalChunks = uploadService.getTotalChunks(fileMd5, userId);
            double progress = calculateProgress(uploadedChunks, totalChunks);



            // 构建数据对象
            Map<String, Object> data = new HashMap<>();
            data.put("uploaded", uploadedChunks);
            data.put("progress", progress);
            data.put("fileName", fileName);
            data.put("fileType", fileType);

            // 构建统一响应格式
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取上传状态成功");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("message", "获取上传状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 合并文件分片接口
     *
     * @param request 包含文件MD5和文件名的请求体
     * @param userId 当前用户ID
     * @return 返回包含合并后文件访问URL的响应
     */
    @Transactional
    @PostMapping("/merge")
    public ResponseEntity<Map<String, Object>> mergeFile(
            @RequestBody MergeRequest request,
            @RequestAttribute("userId") String userId) {


        try {
            String fileType = getFileType(request.fileName());

            // 检查文件完整性和权限
            FileUpload fileUpload = fileUploadRepository.findByFileMd5AndUserId(request.fileMd5(), userId)
                    .orElseThrow(() -> {
                        return new RuntimeException("文件记录不存在");
                    });

            // 确保用户有权限操作该文件
            if (!fileUpload.getUserId().equals(userId)) {

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("code", HttpStatus.FORBIDDEN.value());
                errorResponse.put("message", "没有权限操作此文件");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }


            // 检查分片是否全部上传完成
            List<Integer> uploadedChunks = uploadService.getUploadedChunks(request.fileMd5(), userId);
            int totalChunks = uploadService.getTotalChunks(request.fileMd5(), userId);

            if (uploadedChunks.size() < totalChunks) {

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("code", HttpStatus.BAD_REQUEST.value());
                errorResponse.put("message", "文件分片未全部上传，无法合并");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // 合并文件
            String objectUrl = uploadService.mergeChunks(request.fileMd5(), request.fileName(), userId);

            // 发送任务到 Kafka，包含完整的权限信息

            FileProcessingTask task = new FileProcessingTask(
                    request.fileMd5(),
                    objectUrl,
                    request.fileName(),
                    fileUpload.getUserId(),
                    fileUpload.isPublic()
            );































































            kafkaTemplate.executeInTransaction(kt -> {
                kt.send(kafkaConfig.getFileProcessingTopic(), task);
                return true;
            });

            // 构建数据对象
            Map<String, Object> data = new HashMap<>();
            data.put("object_url", objectUrl);

            // 构建统一响应格式
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "文件合并成功，任务已发送到 Kafka");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("message", "文件合并失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 计算上传进度
     *
     * @param uploadedChunks 已上传的分片列表
     * @param totalChunks 总分片数量
     * @return 返回上传进度的百分比
     */
    private double calculateProgress(List<Integer> uploadedChunks, int totalChunks) {
        if (totalChunks == 0) {
            return 0.0;
        }
        return (double) uploadedChunks.size() / totalChunks * 100;
    }

    /**
     * 合并请求的辅助类，包含文件的MD5值和文件名
     */
    public record MergeRequest(String fileMd5, String fileName) {}

    /**
     * 获取支持的文件类型列表接口
     *
     * @return 返回支持的文件类型信息
     */
    @GetMapping("/supported-types")
    public ResponseEntity<Map<String, Object>> getSupportedFileTypes() {
        try {

            Set<String> supportedTypes = fileTypeValidationService.getSupportedFileTypes();
            Set<String> supportedExtensions = fileTypeValidationService.getSupportedExtensions();

            // 构建数据对象
            Map<String, Object> data = new HashMap<>();
            data.put("supportedTypes", supportedTypes);
            data.put("supportedExtensions", supportedExtensions);
            data.put("description", "系统支持的文档类型文件，这些文件可以被解析并进行向量化处理");

            // 构建统一响应格式
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取支持的文件类型成功");
            response.put("data", data);


            return ResponseEntity.ok(response);
        } catch (Exception e) {

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("message", "获取支持的文件类型失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 根据文件名获取文件类型
     *
     * @param fileName 文件名
     * @return 文件类型
     */
    private String getFileType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "unknown";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "unknown";
        }

        String extension = fileName.substring(lastDotIndex + 1).toLowerCase();

        // 根据文件扩展名返回文件类型
        switch (extension) {
            case "pdf":
                return "PDF文档";
            case "doc":
            case "docx":
                return "Word文档";
            case "xls":
            case "xlsx":
                return "Excel表格";
            case "ppt":
            case "pptx":
                return "PowerPoint演示文稿";
            case "txt":
                return "文本文件";
            case "md":
                return "Markdown文档";
            case "jpg":
            case "jpeg":
                return "JPEG图片";
            case "png":
                return "PNG图片";
            case "gif":
                return "GIF图片";
            case "bmp":
                return "BMP图片";
            case "svg":
                return "SVG图片";
            case "mp4":
                return "MP4视频";
            case "avi":
                return "AVI视频";
            case "mov":
                return "MOV视频";
            case "wmv":
                return "WMV视频";
            case "mp3":
                return "MP3音频";
            case "wav":
                return "WAV音频";
            case "flac":
                return "FLAC音频";
            case "zip":
                return "ZIP压缩包";
            case "rar":
                return "RAR压缩包";
            case "7z":
                return "7Z压缩包";
            case "tar":
                return "TAR压缩包";
            case "gz":
                return "GZ压缩包";
            case "json":
                return "JSON文件";
            case "xml":
                return "XML文件";
            case "csv":
                return "CSV文件";
            case "html":
            case "htm":
                return "HTML文件";
            case "css":
                return "CSS文件";
            case "js":
                return "JavaScript文件";
            case "java":
                return "Java源码";
            case "py":
                return "Python源码";
            case "cpp":
            case "c":
                return "C/C++源码";
            case "sql":
                return "SQL文件";
            default:
                return extension.toUpperCase() + "文件";
        }
    }
}


