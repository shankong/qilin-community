package com.qilin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 「栖邻」后端启动类。
 *
 * 启动：{@code mvn spring-boot:run}，端口见 application.yml（默认 8080）。
 */
@SpringBootApplication
public class QilinApplication {

    public static void main(String[] args) {
        SpringApplication.run(QilinApplication.class, args);
        System.out.println("「栖邻」后端启动成功^_^");
    }
}
