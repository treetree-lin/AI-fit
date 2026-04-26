package org.lin.fitnessapi;

/**
 * @author lin
 * @date 2026-03-25
 */

import org.junit.jupiter.api.Test;
import org.lin.fitnessuser.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisUtils redisUtils;

    @Test
    public void testRedisConnection() {
        // 测试基本操作
        redisUtils.set("test:key", "Hello Redis", 10, TimeUnit.SECONDS);
        Object value = redisUtils.get("test:key");
        System.out.println("获取到的值：" + value);

        // 测试自增
        redisUtils.set("test:counter", 0L);
        Long count = redisUtils.increment("test:counter");
        System.out.println("自增后的值：" + count);
    }
}
