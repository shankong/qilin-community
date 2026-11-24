package com.qilin.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qilin.system.common.Result;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 安全配置类
 * 创建校验过滤链，配置校验规则，处理异常等
 * Configuration 注解：将类标记为配置类，用于定义Bean，启动时先过这个类
 * SecurityConfig
 */
@Configuration
public class SecurityConfig {

    private static final String[] PUBLIC_URLS = {
        "/auth/login",
        "/auth/logout"
    };

    private static final String[] CSRF_IGNORE_URLS = {
        "/auth/login",
        "/auth/logout",
        "/auth/password"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper objectMapper) throws Exception {

        http
            //把登录状态保存到 Session，并在后续请求中读回来
            .securityContext(securityContext ->
                securityContext.securityContextRepository(securityContextRepository())
            )
            // 开启csrf保护，并将public urls添加到忽略列表中，避免csrf保护对public urls生效
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(CSRF_IGNORE_URLS)
            )

            // 关闭security自带的表单登录、http basic登录、注销功能
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)

            // 设置 Session 策略为“需要时创建”，即只有在需要时才会创建 Session。
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            // 配置访问规则，将public urls设置为匿名访问，其他请求都需要认证
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(PUBLIC_URLS).permitAll()
                .anyRequest()
                .authenticated()
            )

            // 配置异常处理，当未登录时返回401错误，包含错误信息：请先登录
            .exceptionHandling(exception -> exception
                // 未登录请求的入口处理器
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    // 把java对象转换为json字符串，写入响应体中
                    objectMapper.writeValue(
                        response.getWriter(),
                        Result.error(401, "请先登录")
                    );
                })
            );
        return http.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
