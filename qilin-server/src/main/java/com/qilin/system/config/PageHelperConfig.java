package com.qilin.system.config;

import com.github.pagehelper.PageInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分页插件配置类
 * proxyBeanMethods = false 表示不使用代理模式，避免循环依赖
 */
@Configuration(proxyBeanMethods = false)
public class PageHelperConfig {
    @Bean
    public PageInterceptor pageInterceptor() {
        return new PageInterceptor();
    }
}
