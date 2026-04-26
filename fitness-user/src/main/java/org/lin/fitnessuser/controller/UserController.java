package org.lin.fitnessuser.controller;
import org.lin.fitnesscommon.utils.MinioUtil;
import org.lin.fitnesscommon.vo.ApiResponse;
import org.lin.fitnessuser.service.UserService;
import org.lin.fitnessuser.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.lin.fitnessuser.dto.LoginRequest;
import org.lin.fitnessuser.dto.ProfileResponse;
import org.lin.fitnessuser.dto.RegisterRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
/**
 * @author lin
 * @date 2026-03-17
 * UserController.java 处理用户相关的所有操作，如登录、注册、获取用户信息等。
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {
   Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
private MinioUtil minioUtil;
/**
 * 注册用户
 */
    @PostMapping("/users/register")
    public ApiResponse<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        System.out.println("用户注册：" + request);
        try {
            Long userId = userService.register(request);
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            return ApiResponse.success("注册成功", data);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("用户名已存在")) {
                return ApiResponse.error("用户名已存在");
            }
            throw e;
        }
    }
    /**
     * 注册管理员（需要管理员权限）
     */
    @PostMapping("/users/register-admin")
    public ApiResponse<Map<String, Object>> registerAdmin(@RequestBody RegisterRequest request) {
        System.out.println("注册管理员：" + request);
        try {
            Long adminId = userService.registerAdmin(request);
            Map<String, Object> data = new HashMap<>();
            data.put("adminId", adminId);
            return ApiResponse.success("管理员注册成功", data);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("用户名已存在")) {
                return ApiResponse.error("用户名已存在");
            }
            throw e;
        }
    }

    /** 用户登录
 * 登录成功后返回 JWT 令牌和用户信息
 */
@PostMapping("/users/login")
public ApiResponse<Map<String, Object>> login(@RequestBody LoginRequest request) {
    System.out.println("用户登录：" + request);
    try {
        Map<String, Object> data = userService.login(request.getUsername(), request.getPassword());
        return ApiResponse.success("登录成功", data);
    } catch (RuntimeException e) {
        if (e.getMessage().contains("用户不存在")) {
            return ApiResponse.error("用户不存在");
        } else if (e.getMessage().contains("密码错误")) {
            return ApiResponse.error("密码错误");
        }
        throw e;
    }
}

    /**
     * 上传用户头像
     */
    @PostMapping("/user/avatar/upload")
    public ApiResponse<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error("上传文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error("不支持的图片格式，请上传图片文件（JPG/PNG/GIF 等）");
            }

            // 验证文件大小（5MB 以内）
            if (file.getSize() > 5 * 1024 * 1024) {
                return ApiResponse.error("头像大小不能超过 5MB");
            }

            // 使用 MinioUtil 上传
            String url = minioUtil.uploadImage(file);

            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("filename", file.getOriginalFilename());
            response.put("size", String.valueOf(file.getSize()));

            logger.info("用户头像上传成功：{}", url);
            return ApiResponse.success("头像上传成功", response);

        } catch (IllegalArgumentException e) {
            logger.error("头像格式错误：{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("头像上传失败", e);
            return ApiResponse.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户头像并保存到数据库
     */
    @PutMapping("/user/avatar/update")
    public ApiResponse<String> updateAvatar(@RequestParam("avatarUrl") String avatarUrl) {
        Long userId = getCurrentUserId();
        logger.info("用户{} 请求更新头像", userId);

        try {
            // 验证 URL 是否为空
            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                return ApiResponse.error("头像 URL 不能为空");
            }

            // 验证 URL 长度（不超过 1000 字符）
            if (avatarUrl.length() > 1000) {
                return ApiResponse.error("头像 URL 过长，请重新上传");
            }

            // 验证 URL 格式（简单的格式检查）
            if (!avatarUrl.startsWith("http://") && !avatarUrl.startsWith("https://")) {
                return ApiResponse.error("无效的头像 URL 格式");
            }

            userService.updateAvatar(userId, avatarUrl);
            logger.info("用户{} 头像更新成功", userId);
            return ApiResponse.success("头像更新成功", "success");
        } catch (Exception e) {
            logger.error("用户{} 头像更新失败: {}", userId, e.getMessage(), e);
            return ApiResponse.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户的画像信息
     * @return
     */
    @GetMapping("/user/profile/me")
    public ApiResponse<Map<String, Object>> getCurrentUserProfile() {
        Long userId = getCurrentUserId();
        System.out.println("用户"+userId+"获取自己的画像");
        
        ProfileResponse profile = userService.getProfile(userId);
        
        // 根据用户角色返回不同的信息
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("profile", profile);
        
        // 如果是管理员，添加额外的认证信息
        if (Boolean.TRUE.equals(profile.getIsAdmin())) {
            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("role", "ADMIN");
            adminInfo.put("permissions", new String[]{
                "manage_users",
                "manage_workouts", 
                "view_all_profiles",
                "delete_content",
                "system_settings"
            });
            adminInfo.put("adminLevel", "SUPER_ADMIN");
            adminInfo.put("canManageOtherUsers", true);
            responseData.put("adminInfo", adminInfo);
            responseData.put("message", "欢迎回来，管理员！");
        } else {
            responseData.put("message", "欢迎回来！");
        }
        
        return ApiResponse.success(responseData);
    }

    /**
     * 获取指定用户的画像信息（需要管理员权限或查看自己的）
     * @param userId
     * @return
     */
    @GetMapping("/admin/profile/{userId}")
    public ApiResponse<ProfileResponse> getProfile(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();

        // 如果不是查看自己的资料，需要检查是否为管理员
        if (!currentUserId.equals(userId)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                throw new RuntimeException("无权查看他人资料");
            }
        }

        //System.out.println("用户"+userId+"获取画像");
        try {
            ProfileResponse profile = userService.getProfile(userId);
            return ApiResponse.success(profile);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("用户不存在")) {
                return ApiResponse.error("用户不存在");
            } else if (e.getMessage().contains("用户画像不存在")) {
                return ApiResponse.error("用户画像不存在");
            }
            throw e;
        }
    }



    /**
 * 更新用户画像信息
 * @param request
 * @return
 */
    @PutMapping("/user/profile/update")
    public ApiResponse<String> updateProfile(@RequestBody RegisterRequest request) {
        System.out.println("用户更新画像"+request);
        Long userId = getCurrentUserId();
        userService.updateProfile(userId, request);
        return ApiResponse.success("更新成功", "success");
    }
/**
 * 获取当前登录用户的 ID
 * @return userId
 */
    private Long getCurrentUserId() {
        // 从 Security 上下文中获取已认证的用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        return userService.getCurrentUserIdByUsername(username);
    }
}

