package org.lin.fitnesscommon.controller;

/**
 * @author lin
 * @date 2026-03-25
 */

import lombok.extern.slf4j.Slf4j;
import org.lin.fitnesscommon.config.MinioProperties;
import org.lin.fitnesscommon.utils.MinioUtil;
import org.lin.fitnesscommon.vo.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传 Controller
 * @author lin
 * @date 2026-03-25
 */
@Slf4j
@RestController
@RequestMapping("/api/common")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private MinioProperties minioProperties;

    /**
     * 上传图片（通用接口）
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    @PostMapping("/upload/image")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error(400, "上传文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error(400, "不支持的图片格式，请上传图片文件（JPG/PNG/GIF 等）");
            }

            // 验证文件大小（10MB 以内）
            if (file.getSize() > 10 * 1024 * 1024) {
                return ApiResponse.error(400, "图片大小不能超过 10MB，当前大小：" + formatFileSize(file.getSize()));
            }

            // 使用 MinioUtil 上传
            String url = minioUtil.uploadImage(file);

            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("filename", file.getOriginalFilename());
            response.put("size", String.valueOf(file.getSize()));

            log.info("图片上传成功：{}", url);
            return ApiResponse.success("上传成功", response);

        } catch (IllegalArgumentException e) {
            log.warn("图片上传参数错误：{}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return ApiResponse.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传视频（通用接口）
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    @PostMapping("/upload/video")
    public ApiResponse<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error(400, "上传文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ApiResponse.error(400, "不支持的视频格式，请上传视频文件（MP4/AVI/MOV 等）");
            }

            // 验证文件大小（100MB 以内）
            if (file.getSize() > 100 * 1024 * 1024) {
                return ApiResponse.error(400, "视频大小不能超过 100MB，当前大小：" + formatFileSize(file.getSize()));
            }

            // 使用 MinioUtil 上传
            String url = minioUtil.uploadVideo(file);

            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("filename", file.getOriginalFilename());
            response.put("size", String.valueOf(file.getSize()));

            log.info("视频上传成功：{}", url);
            return ApiResponse.success("上传成功", response);

        } catch (IllegalArgumentException e) {
            log.warn("视频上传参数错误：{}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("视频上传失败", e);
            return ApiResponse.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传头像（专用接口，限制更严格）
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    @PostMapping("/upload/avatar")
    public ApiResponse<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error(400, "上传文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error(400, "不支持的图片格式");
            }

            // 头像限制 5MB
            if (file.getSize() > 5 * 1024 * 1024) {
                return ApiResponse.error(400, "头像大小不能超过 5MB，当前大小：" + formatFileSize(file.getSize()));
            }

            // 使用 MinioUtil 上传到图片桶
            String url = minioUtil.uploadImage(file);

            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("filename", file.getOriginalFilename());
            response.put("size", String.valueOf(file.getSize()));

            log.info("头像上传成功：{}", url);
            return ApiResponse.success("上传成功", response);

        } catch (IllegalArgumentException e) {
            log.warn("头像上传参数错误：{}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return ApiResponse.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传教程封面（专用接口）
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    @PostMapping("/upload/workout-cover")
    public ApiResponse<Map<String, String>> uploadWorkoutCover(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error(400, "上传文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error(400, "不支持的图片格式");
            }

            // 封面图限制 10MB
            if (file.getSize() > 10 * 1024 * 1024) {
                return ApiResponse.error(400, "封面图大小不能超过 10MB，当前大小：" + formatFileSize(file.getSize()));
            }

            // 使用 MinioUtil 上传到图片桶
            String url = minioUtil.uploadImage(file);

            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("filename", file.getOriginalFilename());
            response.put("size", String.valueOf(file.getSize()));

            log.info("教程封面上传成功：{}", url);
            return ApiResponse.success("上传成功", response);

        } catch (IllegalArgumentException e) {
            log.warn("封面图上传参数错误：{}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("封面图上传失败", e);
            return ApiResponse.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传教程视频（专用接口）
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    @PostMapping("/upload/workout-video")
    public ApiResponse<Map<String, String>> uploadWorkoutVideo(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error(400, "上传文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ApiResponse.error(400, "不支持的视频格式");
            }

            // 教程视频限制 100MB
            if (file.getSize() > 100 * 1024 * 1024) {
                return ApiResponse.error(400, "视频大小不能超过 100MB，当前大小：" + formatFileSize(file.getSize()));
            }

            // 使用 MinioUtil 上传到视频桶
            String url = minioUtil.uploadVideo(file);

            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("filename", file.getOriginalFilename());
            response.put("size", String.valueOf(file.getSize()));

            log.info("教程视频上传成功：{}", url);
            return ApiResponse.success("上传成功", response);

        } catch (IllegalArgumentException e) {
            log.warn("视频上传参数错误：{}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("视频上传失败", e);
            return ApiResponse.error("上传失败：" + e.getMessage());
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
}
