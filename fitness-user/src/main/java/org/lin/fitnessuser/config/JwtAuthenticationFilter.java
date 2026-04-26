package org.lin.fitnessuser.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.lin.fitnessuser.service.CustomUserDetailsService;
import org.lin.fitnessuser.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * @author lin
 * @date 2026-03-17
 *  自定义的过滤器，用于解析请求头中的 JWT Token，并验证用户身份。
 * 如果 Token 有效，则将用户信息和权限设置到 Spring Security 的上下文中，后续的请求可以基于用户角色进行授权。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;//用于生成和解析JWT Token
    @Autowired
    private CustomUserDetailsService userDetailsService; // 加载用户详细信息
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * 每次请求都会调用此方法，用于解析 JWT Token 并设置用户认证信息。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        // 跳过公开路径的认证检查（注册、登录等）
        if (path.startsWith("/api/users/register") || 
            path.startsWith("/api/users/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 从请求头或查询参数中提取 JWT Token（WebSocket 握手时 token 在 URL 参数中）
            String token = extractToken(request);
            if (token == null) {
                token = extractTokenFromQuery(request);
            }
            if (token == null) {
                // 非公开接口必须携带 Token，缺失时返回 401，避免 Spring Security 后续返回 403 造成混淆
                logger.warn("请求缺少有效的 JWT Token: {}", path);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"missing_token\",\"message\":\"请先登录并携带有效的 Token\"}");
                return;
            }

            // 如果请求提供了 token，却校验不通过，立即返回 401
            if (!jwtUtils.validateToken(token)) {
                logger.warn("提供的 token 无效或已过期");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"invalid_or_expired_token\"}");
                return;
            }

            String username = jwtUtils.extractUsernameFromToken(token); // 从 Token 中提取用户名
            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username); // 加载用户详细信息
                // 创建认证对象并设置到 Security 上下文中
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // 记录错误日志并返回 401，避免后续被判为 403
           logger.error("用户JWT认证失败：{}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"authentication_failure\"}");
            return;
        }
        filterChain.doFilter(request, response); // 继续执行过滤链
    }

    /**
     * 从请求头中提取 JWT Token（兼容大小写）
     * 如果提取结果为空字符串，也视为未提供 Token，返回 null
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null) bearerToken = request.getHeader("authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7).trim(); // 去掉 "Bearer " 前缀并去除空白
            return token.isEmpty() ? null : token;
        }
        return null;
    }

    /**
     * 从 URL 查询参数中提取 JWT Token（用于 WebSocket 握手）
     */
    private String extractTokenFromQuery(HttpServletRequest request) {
        String token = request.getParameter("token");
        if (token != null && !token.trim().isEmpty()) {
            return token.trim();
        }
        return null;
    }
}
