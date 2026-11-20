package com.qilin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Redis Session（可选）。
 *
 * <p>默认不生效：只有以 {@code --spring.profiles.active=redis} 启动、
 * 且 Redis 已在运行时，这个配置才会把 Session 存到 Redis。
 *
 * <p>为什么这么做：本机暂时没装 Redis，P0 阶段先用容器内存 Session 跑通；
 * 等部署到服务器（Docker Compose 里带 Redis）时改一个启动参数就能切换，
 * 代码一行不用动——这正是“Session + Redis 支持多实例”的讲法。
 *
 * <p>切换步骤：
 * <pre>
 *   1. 启动 Redis：redis-server
 *   2. 启动后端：mvn spring-boot:run -Dspring-boot.run.profiles=redis
 *   3. 验证：登录后看 Redis 里有没有 qilin:session:* 的 key
 * </pre>
 *
 * <p>注意：切 Redis 后 Session 里的 LoginUser 会被序列化，
 * 所以它实现了 Serializable；将来往里放的对象都要注意这一点。
 */
@Configuration
@Profile("redis")
@EnableRedisHttpSession(
        redisNamespace = "qilin:session",        // Redis key 前缀
        maxInactiveIntervalInSeconds = 7200       // 2 小时，与 application.yml 保持一致
)
public class RedisSessionConfig {

    /**
     * 统一 Session Cookie 的写法。
     * 跨域部署（前端 5173 → 后端 8080）时必须 allowCredentials + 具体域名；
     * HTTPS 环境下要把 setSecure(true) 打开。
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("QILIN_SID");
        serializer.setCookiePath("/");
        serializer.setUseHttpOnlyCookie(true);
        serializer.setSameSite("Lax");
        // serializer.setUseSecureCookie(true);   // 上 HTTPS 后打开
        return serializer;
    }
}
