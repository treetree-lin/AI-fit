package org.lin.fitnesscommon.config;

/**
 * @author lin
 * @date 2026-03-25
 */

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAspectJAutoProxy
@EnableAsync
@ComponentScan("org.lin.fitnesscommon.log")
public class LogAutoConfiguration {
    // 自动配置类，让Spring自动扫描日志相关组件
}