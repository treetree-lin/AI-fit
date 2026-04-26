package org.lin.fitnessuser.service;

/**
 * @author lin
 * @date 2026-03-25
 */
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class JwtTokenStorageService {

    private static final String JWT_TOKEN_PREFIX = "jwt:token:";
    private static final String JWT_BLACKLIST_PREFIX = "jwt:blacklist:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 将 JWT Token 存储到 Redis
     * @param userId 用户 ID
     * @param token JWT Token
     * @param expireTime 过期时间（毫秒）
     */
    public void storeToken(Long userId, String token, long expireTime) {
        String key = JWT_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, expireTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 从 Redis 获取用户的 Token
     * @param userId 用户 ID
     * @return Token 字符串
     */
    public String getToken(Long userId) {
        String key = JWT_TOKEN_PREFIX + userId;
        Object token = redisTemplate.opsForValue().get(key);
        return token != null ? token.toString() : null;
    }

    /**
     * 删除用户的 Token（用于登出）
     * @param userId 用户 ID
     */
    public void deleteToken(Long userId) {
        String key = JWT_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }

    /**
     * 检查 Token 是否在黑名单中
     * @param token JWT Token
     * @return 如果在黑名单中返回 true
     */
    public boolean isTokenBlacklisted(String token) {
        String key = JWT_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 将 Token 加入黑名单（用于登出或使 Token 失效）
     * @param token JWT Token
     * @param expireTime 剩余过期时间（毫秒）
     */
    public void blacklistToken(String token, long expireTime) {
        String key = JWT_BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", expireTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 验证 Token 是否有效（检查是否存在且未过期）
     * @param userId 用户 ID
     * @param token JWT Token
     * @return 如果有效返回 true
     */
    public boolean validateStoredToken(Long userId, String token) {
        String storedToken = getToken(userId);
        return storedToken != null && storedToken.equals(token);
    }
}
