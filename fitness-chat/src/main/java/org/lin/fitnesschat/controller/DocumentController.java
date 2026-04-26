package org.lin.fitnesschat.controller;

/**
 * @author lin
 * @date 2026-04-16
 */

import org.lin.fitnesschat.entity.FileUpload;
import org.lin.fitnesschat.repository.FileUploadRepository;
import org.lin.fitnesschat.service.DocumentService;
import org.lin.fitnessuser.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文档控制器类，处理文档相关操作请求
 */
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private FileUploadRepository fileUploadRepository;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 删除文档及其相关数据
     *
     * @param fileMd5 文件MD5
     * @param userId 当前用户ID
     * @param role 用户角色
     * @return 删除结果
     */
    @DeleteMapping("/{fileMd5}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable String fileMd5,
            @RequestAttribute("userId") String userId,
            @RequestAttribute("role") String role) {

        try {

            // 获取文件信息
            Optional<FileUpload> fileOpt = fileUploadRepository.findByFileMd5AndUserId(fileMd5, userId);
            if (fileOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", HttpStatus.NOT_FOUND.value());
                response.put("message", "文档不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            FileUpload file = fileOpt.get();

            // 权限检查：只有文件所有者或管理员可以删除
            if (!file.getUserId().equals(userId) && !"ADMIN".equals(role)) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", HttpStatus.FORBIDDEN.value());
                response.put("message", "没有权限删除此文档");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // 执行删除操作
            documentService.deleteDocument(fileMd5, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "文档删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "删除文档失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取用户可访问的所有文件列表
     *
     * @param userId 当前用户ID
     * @param orgTags 用户所属组织标签
     * @return 可访问的文件列表
     */
    @GetMapping("/accessible")
    public ResponseEntity<?> getAccessibleFiles(
            @RequestAttribute("userId") String userId,
            @RequestAttribute("orgTags") String orgTags) {

        try {
            List<FileUpload> files = documentService.getAccessibleFiles(userId, orgTags);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取可访问文件列表成功");
            response.put("data", files);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "获取可访问文件列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取用户上传的所有文件列表
     *
     * @param userId 当前用户ID
     * @return 用户上传的文件列表
     */
    @GetMapping("/uploads")
    public ResponseEntity<?> getUserUploadedFiles(
            @RequestAttribute("userId") String userId) {

        try {
            List<FileUpload> files = documentService.getUserUploadedFiles(userId);

            // 将FileUpload转换为包含tagName的DTO
            List<Map<String, Object>> fileData = files.stream().map(file -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("fileMd5", file.getFileMd5());
                dto.put("fileName", file.getFileName());
                dto.put("totalSize", file.getTotalSize());
                dto.put("status", file.getStatus());
                dto.put("userId", file.getUserId());
                dto.put("public", file.isPublic());
                dto.put("createdAt", file.getCreatedAt());
                dto.put("mergedAt", file.getMergedAt());

                return dto;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取用户上传文件列表成功");
            response.put("data", fileData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {

            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "获取用户上传文件列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据文件名下载文件
     *
     * @param fileName 文件名
     * @param token JWT token
     * @return 文件资源或错误响应
     */
    @GetMapping("/download")
    public ResponseEntity<?> downloadFileByName(
            @RequestParam String fileName,
            @RequestParam(required = false) String token) {

        try {
            // 验证token并获取用户信息
            String userId = null;
            String orgTags = null;

            // 优先从Spring Security上下文获取已认证的用户信息
            try {
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()
                        && authentication.getPrincipal() instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    userId = userDetails.getUsername();
                    // 从userDetails中获取组织标签信息
                    orgTags = userDetails.getAuthorities().stream()
                            .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                            .findFirst()
                            .orElse(null);
                }
            } catch (Exception e) {
            }

            // 如果Security上下文中没有用户信息，尝试从URL参数token中获取
            if (userId == null && token != null && !token.trim().isEmpty()) {
                try {
                    // 解析JWT token获取用户信息
                    userId = jwtUtils.extractUsernameFromToken(token);
                } catch (Exception e) {
                }
            }


            // 如果没有提供token或token无效，只允许下载公开文件
            if (userId == null) {
                // 查找公开文件
                Optional<FileUpload> publicFile = fileUploadRepository.findByFileNameAndIsPublicTrue(fileName);
                if (publicFile.isEmpty()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", HttpStatus.NOT_FOUND.value());
                    response.put("message", "文件不存在或需要登录访问");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }

                FileUpload file = publicFile.get();
                String downloadUrl = documentService.generateDownloadUrl(file.getFileMd5());

                if (downloadUrl == null) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
                    response.put("message", "无法生成下载链接");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }

                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "文件下载链接生成成功");
                response.put("data", Map.of(
                        "fileName", file.getFileName(),
                        "downloadUrl", downloadUrl,
                        "fileSize", file.getTotalSize()
                ));
                return ResponseEntity.ok(response);
            }

            // 有token的情况，查找用户可访问的文件
            List<FileUpload> accessibleFiles = documentService.getAccessibleFiles(userId, orgTags);

            // 根据文件名查找匹配的文件
            Optional<FileUpload> targetFile = accessibleFiles.stream()
                    .filter(file -> file.getFileName().equals(fileName))
                    .findFirst();

            if (targetFile.isEmpty()) {

                Map<String, Object> response = new HashMap<>();
                response.put("code", HttpStatus.NOT_FOUND.value());
                response.put("message", "文件不存在或无权限访问");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            FileUpload file = targetFile.get();

            // 生成下载链接或返回预签名URL
            String downloadUrl = documentService.generateDownloadUrl(file.getFileMd5());

            if (downloadUrl == null) {

                Map<String, Object> response = new HashMap<>();
                response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.put("message", "无法生成下载链接");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }



            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "文件下载链接生成成功");
            response.put("data", Map.of(
                    "fileName", file.getFileName(),
                    "downloadUrl", downloadUrl,
                    "fileSize", file.getTotalSize()
            ));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            String userId = "unknown";
            try {
                if (token != null && !token.trim().isEmpty()) {
                    userId = jwtUtils.extractUsernameFromToken(token);
                }
            } catch (Exception ignored) {}


            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "文件下载失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 预览文件内容
     *
     * @param fileName 文件名
     * @param token JWT token (URL参数，用于向后兼容)
     * @return 文件预览内容或错误响应
     */
    @GetMapping("/preview")
    public ResponseEntity<?> previewFileByName(
            @RequestParam String fileName,
            @RequestParam(required = false) String token) {

        try {
            // 验证token并获取用户信息
            String userId = null;
            String orgTags = null;

            // 优先从Spring Security上下文获取已认证的用户信息
            try {
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated()
                        && authentication.getPrincipal() instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    userId = userDetails.getUsername();
                    // 从userDetails中获取组织标签信息
                    orgTags = userDetails.getAuthorities().stream()
                            .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                            .findFirst()
                            .orElse(null);
                }
            } catch (Exception e) {
            }

            // 如果Security上下文中没有用户信息，尝试从URL参数token中获取
            if (userId == null && token != null && !token.trim().isEmpty()) {
                try {
                    userId = jwtUtils.extractUsernameFromToken(token);
                } catch (Exception e) {
                }
            }


            // 如果没有提供token或token无效，只允许预览公开文件
            if (userId == null) {
                Optional<FileUpload> publicFile = fileUploadRepository.findByFileNameAndIsPublicTrue(fileName);
                if (publicFile.isEmpty()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", HttpStatus.NOT_FOUND.value());
                    response.put("message", "文件不存在或需要登录访问");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }

                FileUpload file = publicFile.get();
                String previewContent = documentService.getFilePreviewContent(file.getFileMd5(), file.getFileName());

                if (previewContent == null) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
                    response.put("message", "无法获取文件预览内容");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }

                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "文件预览内容获取成功");
                response.put("data", Map.of(
                        "fileName", file.getFileName(),
                        "content", previewContent,
                        "fileSize", file.getTotalSize()
                ));
                return ResponseEntity.ok(response);
            }

            // 有token的情况，查找用户可访问的文件
            List<FileUpload> accessibleFiles = documentService.getAccessibleFiles(userId, orgTags);

            // 根据文件名查找匹配的文件
            Optional<FileUpload> targetFile = accessibleFiles.stream()
                    .filter(file -> file.getFileName().equals(fileName))
                    .findFirst();

            if (targetFile.isEmpty()) {

                Map<String, Object> response = new HashMap<>();
                response.put("code", HttpStatus.NOT_FOUND.value());
                response.put("message", "文件不存在或无权限访问");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            FileUpload file = targetFile.get();

            // 获取文件预览内容
            String previewContent = documentService.getFilePreviewContent(file.getFileMd5(), file.getFileName());

            if (previewContent == null) {

                Map<String, Object> response = new HashMap<>();
                response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.put("message", "无法获取文件预览内容");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }



            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "文件预览内容获取成功");
            response.put("data", Map.of(
                    "fileName", file.getFileName(),
                    "content", previewContent,
                    "fileSize", file.getTotalSize()
            ));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            String userId = "unknown";
            try {
                if (token != null && !token.trim().isEmpty()) {
                    userId = jwtUtils.extractUsernameFromToken(token);
                }
            } catch (Exception ignored) {}


            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "文件预览失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


}
