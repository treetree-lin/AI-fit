package org.lin.fitnesschat.config;

import org.lin.fitnesschat.interceptor.UserAttributeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 注册用户属性拦截器，为 Controller 提供 @RequestAttribute 注入支持
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserAttributeInterceptor userAttributeInterceptor;

    @Autowired
    public WebMvcConfig(UserAttributeInterceptor userAttributeInterceptor) {
        this.userAttributeInterceptor = userAttributeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userAttributeInterceptor)
                .addPathPatterns("/api/**", "/api/upload/**")
                .excludePathPatterns("/api/users/register", "/api/users/login");
    }
}
