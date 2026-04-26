package org.lin.fitnessuser.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.lin.fitnesscommon.entity.User;
import org.lin.fitnessuser.repository.UserRepository;
import org.lin.fitnessuser.service.JwtTokenStorageService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.*;

/**
 * @author lin
 * @date 2026-03-17
 * JwtUtils.java 负责 JWT 令牌的生成、解析和验证，是无状态认证的核心。
 */

@Component
public class JwtUtils {
    Logger logger = org.slf4j.LoggerFactory.getLogger(JwtUtils.class);
    // JWT 密钥从配置文件中获取（Base64 编码）
    @Value("${jwt.secret-key}")
    private  String SECRET_KEY;
    private static final long EXPIRATION_TIME = 86400000; // 24 hours
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenStorageService jwtTokenStorageService;

    /**
     * 解析Base64编码的密钥，并返回 SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token并存储到Redis 中
     *
     * @param username 用户名
     * @return 生成的 JWT Token
     */
    public String generateJWTToken(String username) {
        // 获取用户信息
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId().toString()); // 添加用户ID到JWT

        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_TIME);

        String token=Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
        jwtTokenStorageService.storeToken(user.getId(), token, expiry.getTime());
        return token;
    }

    /**
     * 提取Claims，忽略过期异常
     */
    private Claims extractClaimsIgnoreExpiration(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 忽略过期异常，返回claims
            return e.getClaims();
        } catch (Exception e) {
            logger.debug("无法从token中提取 claims：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 提取Claims（正常验证）
     */
    private Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
/**
 106→     * 验证 JWT Token 是否有效（增加 Redis 验证）
 107→     *
 108→     * @param token JWT Token
 109→     * @return 如果 Token 有效则返回 true，否则返回 false
 110→     */
      public boolean validateTokenWithRedis(String token) {
          if (!validateToken(token)) {
                        return false;
          }

               String userId = extractUserIdFromToken(token);
               if (userId == null) {
                     return false;
                  }

               if (jwtTokenStorageService.isTokenBlacklisted(token)) {
                       logger.warn("Token 已在黑名单中");
                     return false;
                   }

             return jwtTokenStorageService.validateStoredToken(Long.parseLong(userId), token);
          }
    /**
     * 验证 JWT Token 是否有效
     *
     * @param token JWT Token
     * @return 如果 Token 有效则返回 true，否则返回 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("Token已过期: {}", e.getClaims() != null ? e.getClaims().get("userId", String.class) : "");
        } catch (SignatureException e) {
            logger.warn("无效的token签名");
        } catch (Exception e) {
            logger.error("token验证出错", e);
        }
        return false;
    }

    /**
     * 从 JWT Token 中提取用户ID
     */
    public String extractUserIdFromToken(String token) {
        try {
            Claims claims = extractClaimsIgnoreExpiration(token);
            return claims != null ? claims.get("userId", String.class) : null;
        } catch (Exception e) {
            logger.error("从token中提取用户ID时出错: {}", token, e);
            return null;
        }
    }

    /**
     * 从 JWT Token 中提取用户名
     * @param token JWT Token
     * @return 用户名
     */
    public String extractUsernameFromToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims != null ? claims.getSubject() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 JWT Token 中提取用户角色
     */
    public String extractRoleFromToken(String token) {
        try {
            Claims claims = extractClaimsIgnoreExpiration(token);
            return claims != null ? claims.get("role", String.class) : null;
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * 登出：将Token加入黑名单
     * @param token JWT Token
     */
    public void logout(String token) {
        if(token != null)
        {
            String userId = extractUserIdFromToken(token);
            if(userId != null)
            {
                jwtTokenStorageService.deleteToken(Long.parseLong(userId));
            }
            long remainingTime=getRemainTime(token);
            if(remainingTime>0)
            {
                jwtTokenStorageService.blacklistToken(token, remainingTime);
            }
        }
    }
    /**
     * 获取剩余过期时间
     * @param token JWT Token
     * @return 剩余过期时间（毫秒）
     */
    private long getRemainTime(String token)
    {
        try {
            Claims claims = extractClaimsIgnoreExpiration(token);
            if(claims != null)
            {
                Date expiration = claims.getExpiration();
                if(expiration != null)
                {
                    long expireTime=expiration.getTime();
                    long now=System.currentTimeMillis();
                    return expireTime-now;
                }
            }
        } catch (Exception e) {
            logger.error("获取 token 剩余时间失败", e);        }
        return 0;
    }

}