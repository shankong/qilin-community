package com.qilin.module.auth.controller;

import com.qilin.common.result.Result;
import com.qilin.module.auth.dto.ChangePasswordRequest;
import com.qilin.module.auth.dto.LoginRequest;
import com.qilin.module.auth.dto.UserInfoVO;
import com.qilin.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 *
 * <p>统一前缀 /api/v1；登录/验证码在 WebMvcConfig 里被排除在登录校验之外，
 * 其余接口都必须先登录。
 */
@Tag(name = "01. 认证")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录", description = "成功后服务端写入 Session，并返回 QILIN_SID Cookie")
    @PostMapping("/login")
    public Result<UserInfoVO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return Result.ok(authService.login(request, httpRequest));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest httpRequest) {
        authService.logout(httpRequest);
        return Result.ok();
    }

    @Operation(summary = "当前登录用户信息", description = "含角色与按钮级权限列表，前端据此渲染菜单/按钮")
    @GetMapping("/me")
    public Result<UserInfoVO> me() {
        return Result.ok(authService.me());
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return Result.ok();
    }
}
