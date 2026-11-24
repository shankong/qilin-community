package com.qilin.system.service.impl;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qilin.system.service.AuthService;
import com.qilin.system.vo.AuthLoginVO;

import jakarta.annotation.Resource;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qilin.system.dto.request.AuthLoginRequest;
import com.qilin.system.dto.request.AuthUpdatePasswordRequest;
import com.qilin.system.entity.SysUser;
import com.qilin.system.exception.BusinessException;
import com.qilin.system.exception.InvalidCredentialsException;
import com.qilin.system.mapper.AuthLoginMapper;

/**
 * 认证服务实现类
 * AuthServiceImpl
 */
@Service 
public class AuthServiceImpl implements AuthService {

    @Resource 
    private AuthLoginMapper authLoginMapper;

    @Resource 
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 登录认证
     * @param request
     * @return 登录成功后的用户信息
     */
    @Override 
    public AuthLoginVO authLogin(AuthLoginRequest request) {
        /**
         * 这里不需要校验请求参数是否为空
         * 因为在Controller层已经校验过了
         * @Valid @RequestBody 注解会自动校验请求参数是否为空
         */
        String username = request.getUsername();
        String password = request.getPassword();

        // 拿到username查询数据库
        SysUser user = authLoginMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                                                    .eq(SysUser::getUsername, username)
                                                    .eq(SysUser::getDeleted, 0)
                                                    .eq(SysUser::getStatus, 1)
        );                 
        // 用户名不存在或密码错误直接抛出异常
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
             throw new InvalidCredentialsException();
        }

        return AuthLoginVO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .avatar(user.getAvatar())
                            .status(user.getStatus())
                            .lastLoginAt(user.getLastLoginAt())
                            .createdAt(user.getCreatedAt())
                            .updatedAt(user.getUpdatedAt())
                            .build();
    }

    /**
     * 更改密码
     * @param request
     * @return 更改后的用户信息
     */
    @Override
    @Transactional 
    public void updatePassword(Long userId, AuthUpdatePasswordRequest request) {
        SysUser user = authLoginMapper.selectById(userId);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        authLoginMapper.updateById(user);
    }
}
