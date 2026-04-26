package org.lin.fitnesscommon.exception;

/**
 * @author lin
 * @date 2026-03-25
 */

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.lin.fitnesscommon.vo.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "org.lin.fitnessuser.controller")
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 处理 JWT 过期异常
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredJwtException(
            ExpiredJwtException e, WebRequest request) {
        logger.warn("JWT Token 已过期：{}", e.getMessage());

        Map<String, Object> body = createErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "登录已过期，请重新登录",
            request.getDescription(false).replace("uri=", ""),
            "TOKEN_EXPIRED"
        );

        body.put("errorCode", "AUTH_001");
        body.put("suggestion", "请使用用户名密码重新登录获取新 token");

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 处理 JWT 签名异常
     */
    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<Map<String, Object>> handleSignatureException(
            SignatureException e, WebRequest request) {
        logger.error("JWT 签名验证失败：{}", e.getMessage());

        Map<String, Object> body = createErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "无效的登录令牌",
            request.getDescription(false).replace("uri=", ""),
            "INVALID_SIGNATURE"
        );

        body.put("errorCode", "AUTH_002");
        body.put("suggestion", "请清除缓存后重新登录");

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 处理 JWT 格式异常
     */
    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJwtException(
            MalformedJwtException e, WebRequest request) {
        logger.error("JWT Token 格式错误：{}", e.getMessage());

        Map<String, Object> body = createErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "令牌格式错误",
            request.getDescription(false).replace("uri=", ""),
            "MALFORMED_TOKEN"
        );

        body.put("errorCode", "AUTH_003");
        body.put("suggestion", "请检查 Authorization 头格式是否正确");

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 处理认证凭证未找到异常
     */
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCredentialsNotFoundException(
            AuthenticationCredentialsNotFoundException e, WebRequest request) {
        logger.warn("未提供认证凭证：{}", e.getMessage());

        Map<String, Object> body = createErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "请先登录系统",
            request.getDescription(false).replace("uri=", ""),
            "CREDENTIALS_NOT_FOUND"
        );

        body.put("errorCode", "AUTH_004");
        body.put("suggestion", "请在请求头中添加 Authorization: Bearer {token}");

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 处理其他认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException e, WebRequest request) {
        logger.error("认证失败：{}", e.getMessage());

        Map<String, Object> body = createErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "认证失败：" + e.getMessage(),
            request.getDescription(false).replace("uri=", ""),
            "AUTHENTICATION_FAILED"
        );

        body.put("errorCode", "AUTH_999");

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException e, WebRequest request) {
        logger.warn("权限不足：{} - {}", request.getDescription(false), e.getMessage());

        Map<String, Object> body = createErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "权限不足，无法访问该资源",
            request.getDescription(false).replace("uri=", ""),
            "ACCESS_DENIED"
        );

        body.put("errorCode", "AUTH_005");
        body.put("requiredRole", extractRequiredRole(e.getMessage()));
        body.put("suggestion", "请联系管理员获取相应权限");

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * 处理业务运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException e, WebRequest request) {
        String message = e.getMessage();

        logger.error("运行时异常：{}", message);

        HttpStatus status = determineHttpStatus(message);
        Map<String, Object> body = createErrorResponse(
            status.value(),
            getFriendlyMessage(message),
            request.getDescription(false).replace("uri=", ""),
            "BUSINESS_ERROR"
        );

        body.put("errorCode", "BIZ_" + status.value());

        if (message.contains("用户不存在")) {
            body.put("suggestion", "请检查用户 ID 是否正确");
        } else if (message.contains("已存在")) {
            body.put("suggestion", "请使用其他名称重试");
        }

        return new ResponseEntity<>(body, status);
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception e, WebRequest request) {
        logger.error("系统内部异常", e);

        Map<String, Object> body = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "系统内部错误，请稍后重试",
            request.getDescription(false).replace("uri=", ""),
            "INTERNAL_ERROR"
        );

        body.put("errorCode", "SYS_500");
        body.put("suggestion", "如问题持续，请联系技术支持");

        // 开发环境显示详细错误信息
        if (isDevelopmentEnvironment()) {
            Map<String, String> debug = new LinkedHashMap<>();
            debug.put("exceptionType", e.getClass().getName());
            debug.put("message", e.getMessage());
            body.put("debug", debug);
        }

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 创建统一的错误响应体
     */
    private Map<String, Object> createErrorResponse(int status, String message,
                                                     String path, String errorType) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().format(formatter));
        body.put("status", status);
        body.put("error", getErrorName(status));
        body.put("message", message);
        body.put("path", path);
        body.put("type", errorType);
        return body;
    }

    /**
     * 获取错误名称
     */
    private String getErrorName(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown Error";
        };
    }

    /**
     * 根据错误消息确定 HTTP 状态码
     */
    private HttpStatus determineHttpStatus(String message) {
        if (message.contains("不存在")) {
            return HttpStatus.NOT_FOUND;
        } else if (message.contains("已存在") || message.contains("重复")) {
            return HttpStatus.CONFLICT;
        } else if (message.contains("无权") || message.contains("权限")) {
            return HttpStatus.FORBIDDEN;
        } else if (message.contains("参数") || message.contains("格式")) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * 获取友好的错误消息
     */
    private String getFriendlyMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "操作失败";
        }

        if (message.contains("用户不存在")) {
            return "用户不存在";
        } else if (message.contains("用户名已存在")) {
            return "用户名已被使用";
        } else if (message.contains("密码错误")) {
            return "密码错误";
        } else if (message.contains("无权")) {
            return "无权访问该资源";
        } else if (message.contains("画像不存在")) {
            return "用户画像信息不存在";
        }

        return message;
    }

    /**
     * 从异常消息中提取所需角色
     */
    private String extractRequiredRole(String message) {
        if (message.contains("ROLE_ADMIN")) {
            return "ADMIN";
        } else if (message.contains("ROLE_USER")) {
            return "USER";
        }
        return "UNKNOWN";
    }

    /**
     * 判断是否为开发环境
     */
    private boolean isDevelopmentEnvironment() {
        String activeProfile = System.getProperty("spring.profiles.active",
                                     System.getenv("SPRING_PROFILES_ACTIVE"));
        return activeProfile == null || "dev".equalsIgnoreCase(activeProfile)
               || "local".equalsIgnoreCase(activeProfile);
    }
}

