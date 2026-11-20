package com.qilin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 3 文档配置。
 *
 * <p>启动后访问 <a href="http://localhost:8080/doc.html">http://localhost:8080/doc.html</a>。
 * 登录接口可以先在文档里调，浏览器会自动保存 Cookie，
 * 之后就能直接调受保护接口——这就是 P0 阶段 Postman 的替代品。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI qilinOpenApi() {
        return new OpenAPI().info(new Info()
                .title("「栖邻」社区物业工单协同平台 API")
                .description("统一前缀 /api/v1，统一返回 { code, msg, data, traceId }。"
                        + "认证方式：Session + Cookie（QILIN_SID）。")
                .version("v0.1.0")
                .contact(new Contact().name("栖邻项目")));
    }

    @Bean
    public GroupedOpenApi apiV1() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
