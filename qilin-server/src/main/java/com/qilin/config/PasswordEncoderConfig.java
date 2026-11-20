package com.qilin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密器。
 *
 * <p>为什么用 BCrypt 而不是 MD5：
 * BCrypt 自带随机盐、计算慢（可调 cost），同样的密码每次加密结果都不同，
 * 彩虹表打不动；MD5 已经属于“不能写进简历”的做法。
 *
 * <p>为什么只引 spring-security-crypto：
 * 只需要一个 PasswordEncoder，没必要把整个 Spring Security 过滤器链引进来。
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 默认 cost=10，单次约 60-100ms，登录接口可接受
        return new BCryptPasswordEncoder();
    }
}
