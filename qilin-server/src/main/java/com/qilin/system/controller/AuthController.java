package com.qilin.system.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.qilin.system.common.Result;
import com.qilin.system.dto.request.AuthLoginRequest;
import com.qilin.system.service.AuthService;
import com.qilin.system.vo.AuthLoginVO;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/auth")
public class AuthController {
    @Resource 
    private AuthService authService;
    
    /**
     * 登陆接口
     * @param request 登陆请求参数
     * @param servletRequest HTTP请求对象
     * @return 登陆成功后的用户信息
     */
    @PostMapping("/login")
    public Result<AuthLoginVO> login(
            @Valid @RequestBody AuthLoginRequest request, 
            HttpServletRequest servletRequest) {
        AuthLoginVO user = authService.authLogin(request);

        /**
         * 查找用户的session
         * 如果存在则更新session中的用户信息
         * 如果不存在则创建新的session
         */
        HttpSession session = servletRequest.getSession(true);
        /**
         * 登陆成功后，更新session id
         * 避免session id被猜测，导致安全问题
         */
        servletRequest.changeSessionId();
        /**
         * 登陆成功后，将用户id存储到session中
         * 用于后续的权限校验
         * 例如：在拦截器中校验用户是否登录
         */
        session.setAttribute("LOGIN_USER_ID", user.getId());
        
        return Result.success(user);
    }

    /**
     * 获取当前登录用户信息
     * @param userId 当前登录用户的id
     * @return 用户信息
     * @return 用户id
     */
    @GetMapping("/me")
    public ResponseEntity<Result<Long>> me(
        @SessionAttribute(name = "LOGIN_USER_ID", required = false) Long userId
    ) {
        if(userId == null){
             return ResponseEntity.status(401).body(Result.error(401, "请先登录"));
        }
        return ResponseEntity.ok(Result.success(userId));
    }

    /**
     * 退出登录接口
     * @param servletRequest
     * @return
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession(false);

        if(session != null){
            // 退出登录后，将session中的用户信息清除
            session.invalidate();
        }
        return Result.success();
    }
}
