package org.lin.fitnesscommon.constant;

/**
 * @author lin
 * @date 2026-03-25
 */
public class RedisConstants {

    // 教程统计数据缓存
    public static final String WORKOUT_STATS_KEY_PREFIX = "workout:stats:";

    // 用户收藏状态缓存
    public static final String USER_FAVORITE_KEY_PREFIX = "user:favorite:";

    // 评论点赞状态缓存
    public static final String COMMENT_LIKE_KEY_PREFIX = "comment:like:";

    // 缓存过期时间（天）
    public static final long STATS_CACHE_EXPIRE_DAYS = 7;

    // 收藏状态缓存过期时间（天）
    public static final long FAVORITE_CACHE_EXPIRE_DAYS = 30;

    // 评论点赞缓存过期时间（天）
    public static final long COMMENT_LIKE_CACHE_EXPIRE_DAYS = 30;
}
