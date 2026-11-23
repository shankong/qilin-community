package com.qilin.system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.qilin.system.interceptor.LoginInterceptor;

/**
 * WebMvc 配置类
 * 用于配置 WebMvc 相关的拦截器、过滤器等
 * 例如：登录拦截器、权限校验过滤器等
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 创建登录拦截器
     */
    private final LoginInterceptor loginInterceptor;

    public WebMvcConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 把 loginInterceptor 注册到 Spring MVC。之后请求进入 Controller 前，Spring 会调用这个拦截器。
        registry.addInterceptor(loginInterceptor)
                // 配置拦截器生效的路径模式
                .addPathPatterns("/**")
                // 配置拦截器不生效的路径模式
                .excludePathPatterns("/auth/login");
    }
}
