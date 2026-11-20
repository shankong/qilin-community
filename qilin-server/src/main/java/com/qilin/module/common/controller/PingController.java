package com.qilin.module.common.controller;

import com.qilin.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查（免登录）。
 *
 * <p>P0 第一天的验收动作：浏览器打开 http://localhost:8080/api/v1/ping
 * 能看到 db = UP，说明“后端 → MySQL”这条线通了。
 */
@Tag(name = "00. 健康检查")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PingController {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;

    @Operation(summary = "连通性检查", description = "返回服务时间与数据库时间")
    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("app", "qilin-server");
        data.put("status", "UP");
        data.put("serverTime", LocalDateTime.now().format(TIME_FORMAT));

        try {
            String dbTime = jdbcTemplate.queryForObject(
                    "SELECT DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s')", String.class);
            data.put("db", "UP");
            data.put("dbTime", dbTime);
        } catch (DataAccessException e) {
            data.put("db", "DOWN");
        }
        return Result.ok(data);
    }
}
