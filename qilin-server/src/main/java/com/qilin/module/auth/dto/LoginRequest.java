package com.qilin.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录入参。
 *
 * <p>校验注解放在 DTO 上，Controller 加 @Valid，
 * 校验失败由 GlobalExceptionHandler 统一转成 400 + 中文提示。
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名过长")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需为 6-32 位")
    private String password;
}
