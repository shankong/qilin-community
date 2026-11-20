package com.qilin.system.vo;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder 
public class AuthLoginVO {
    private Long id;
    private String username;
    private String avatar; // 头像
    private Integer status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
