package com.qilin.system.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.qilin.system.common.Result;
import com.qilin.system.dto.request.AuthLoginRequest;
import com.qilin.system.service.AuthService;
import com.qilin.system.vo.AuthLoginVO;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Resource 
    private AuthService authService;
    
    @RequestMapping("/login")
    public Result<AuthLoginVO> login(@Valid @RequestBody AuthLoginRequest request) {
        return Result.success(authService.authLogin(request));
    }
}
