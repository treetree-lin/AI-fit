package org.lin.fitnesscommon.utils;

/**
 * @author lin
 * @date 2026-03-25
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置 Hash 结构数据
     * Redis 的 Hash（哈希）数据类型中，数据存储采用三层结构：key field value
     */
    public void setHash(String key, Map<String, Object> map, long expireTime, TimeUnit timeUnit) {
        if (map != null && !map.isEmpty()) {
            redisTemplate.opsForHash().putAll(key, map);
        }
        if (expireTime > 0) {
            redisTemplate.expire(key, expireTime, timeUnit);
        }
    }

    /**
     * 获取 Hash 结构数据
     */
    public Map<Object, Object> getHash(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取 Hash 单个字段值
     */
    public Object getHashValue(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 设置 Hash 单个字段值
     */
    public void setHashValue(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 递增 Hash 中的数值
     */
    public Long incrementHash(String key, String field, long delta) {
        return redisTemplate.opsForHash().increment(key, field, delta);
    }

    /**
     * 删除 Key
     */
    public Boolean deleteKey(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断 Key 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置 String 类型数据
     */
    public void setValue(String key, Object value, long expireTime, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
    }

    /**
     * 获取 String 类型数据
     */
    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取 Boolean 值
     */
    public Boolean getBooleanValue(String key) {
        Object value = getValue(key);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    /**
     * 设置 Boolean 值
     */
    public void setBooleanValue(String key, Boolean value, long expireTime, TimeUnit timeUnit) {
        setValue(key, value, expireTime, timeUnit);
    }

    /**
     * 删除指定前缀的所有 Key
     */
    public void deleteKeysByPrefix(String prefix) {
        // 注意：生产环境慎用 keys 命令，可能影响性能
        // 建议使用 Lua 脚本或其他方式
    }
}
