package org.lin.fitnessuser.config;

/**
 * @author lin
 * @date 2026-03-17
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 配置Spring Security的类
 * 该类定义了应用的安全配置，包括请求的授权规则、CSRF保护的配置以及会话管理策略
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 日志记录器，用于记录安全配置的相关信息
   private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置SecurityFilterChain bean的方法
     * 该方法主要用于配置应用的安全规则，包括哪些请求需要授权、CSRF保护的启用或禁用、会话管理策略等
     *
     * @param http HttpSecurity对象，用于配置应用的安全规则
     * @return SecurityFilterChain对象，代表配置好的安全过滤链
     * @throws Exception 如果配置过程中发生错误，会抛出异常
     */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        try {
            // 启用 CORS 并禁用 CSRF 保护
            http.cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                    // 配置请求的授权规则
                    .authorizeHttpRequests(authorize -> authorize
                            // 公开接口
                            .requestMatchers("/api/users/**").permitAll()
                            // 通用上传接口，需要登录
                            .requestMatchers("/api/common/upload/**").hasAnyRole("USER", "ADMIN")
                            // 健身教程管理，需要 ADMIN
                            .requestMatchers(HttpMethod.POST, "/api/workouts").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/workouts/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/workouts/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.PATCH, "/api/workouts/**").hasRole("ADMIN")
                            // 运动记录相关接口，需要登录
                            .requestMatchers("/api/records/**").hasAnyRole("USER", "ADMIN")
                            // 管理后台接口
                            .requestMatchers("/api/admin/**").hasRole("ADMIN")
                            // 用户相关接口，需要登录
                            .requestMatchers("/api/user/**").hasAnyRole("USER","ADMIN")
                            .anyRequest().authenticated())
                    // 配置会话管理策略
                    // 设置会话创建策略为STATELESS，表示不会创建会话，通常用于无状态的API应用
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                               .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
          logger.info("安全配置加载成功.");
            // 返回配置好的安全过滤链
            return http.build();
        } catch (Exception e) {
            logger.error("配置安全过滤链失败", e);
            // 抛出异常，以便外部处理
            throw e;
        }
    }

    /**
     * 提供跨域配置，允许前端发起带 Authorization 头的请求和预检请求
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
