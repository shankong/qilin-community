package com.qilin.system.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qilin.system.common.Result;
import com.qilin.system.dto.request.AuthLoginRequest;
import com.qilin.system.dto.request.AuthUpdatePasswordRequest;
import com.qilin.system.service.AuthService;
import com.qilin.system.vo.AuthLoginVO;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Resource 
    private AuthService authService;

    @Resource
    private SecurityContextRepository securityContextRepository;
    
    /**
     * 登陆接口
     * @param request 登陆请求参数
     * @param servletRequest HTTP请求对象
     * @return 登陆成功后的用户信息
     */
    @PostMapping("/login")
    public Result<AuthLoginVO> login(
            @Valid @RequestBody AuthLoginRequest request, 
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {

        AuthLoginVO user = authService.authLogin(request);
        /**
         * 登陆成功后，更新session id
         * 避免session id被猜测，导致安全问题
         */
        servletRequest.changeSessionId();

        List<String> roleCodes = authService.findRoleCodes(user.getId());

        List<SimpleGrantedAuthority> authorities = roleCodes.stream()
            .map(roleCode -> new SimpleGrantedAuthority("ROLE_" + roleCode))
            .collect(Collectors.toList());
        
        // 把当前用户放进 Spring Security 的认证上下文
        Authentication authentication = 
            UsernamePasswordAuthenticationToken.authenticated(user.getId(), null, authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 保存信息到session，用于后续的权限校验
        securityContextRepository.saveContext(context, servletRequest, servletResponse);
        
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
        Authentication authentication
    ) {
        if(authentication == null){
             return ResponseEntity.status(401).body(Result.error(401, "请先登录"));
        }
        return ResponseEntity.ok(Result.success((Long) authentication.getPrincipal()));
    }

    /**
     * 退出登录接口
     * @param servletRequest
     * @return 退出登录成功后的用户信息
     */
    @PostMapping("/logout")
    public Result<Void> logout(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return Result.success();
    }

    /**
     * 更新密码接口
     * @param userId
     * @param request
     * @return 更新密码成功后的用户信息
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(
        Authentication authentication,
        HttpServletRequest request,
        HttpServletResponse response,
        @Valid @RequestBody AuthUpdatePasswordRequest authUpdatePasswordRequest
    ) {
        authService.updatePassword((Long)authentication.getPrincipal(), authUpdatePasswordRequest);
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return Result.success();
    }
}
