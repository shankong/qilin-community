package com.qilin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 「栖邻」后端启动类。
 *
 * <p>启动前确认：MySQL 已运行、qilin 库已建（sql/qilin_init.sql）。
 * 启动后：
 * <ul>
 *   <li>接口文档：http://localhost:8080/doc.html</li>
 *   <li>健康检查：http://localhost:8080/api/v1/ping</li>
 * </ul>
 *
 * <p>说明：Mapper 接口统一用 {@code @Mapper} 标注，所以这里不需要 @MapperScan。
 */
@SpringBootApplication
public class QilinApplication {

    public static void main(String[] args) {
        SpringApplication.run(QilinApplication.class, args);
    }
}
