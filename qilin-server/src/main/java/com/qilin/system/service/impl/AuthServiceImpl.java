package com.qilin.system.service.impl;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.qilin.system.service.AuthService;
import com.qilin.system.vo.AuthLoginVO;

import jakarta.annotation.Resource;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qilin.system.dto.request.AuthLoginRequest;
import com.qilin.system.entity.SysUser;
import com.qilin.system.mapper.AuthLoginMapper;

@Service 
public class AuthServiceImpl implements AuthService {

    @Resource 
    private AuthLoginMapper authLoginMapper;

    @Override 
    public AuthLoginVO authLogin(AuthLoginRequest request) {
        // 请求参数为空直接抛出异常
        if (request == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }

        String username = request.getUsername();
        String password = request.getPassword();

        // 拿到username查询数据库
        SysUser user = authLoginMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                                                    .eq(SysUser::getUsername, username)
                                                    .eq(SysUser::getDeleted, 0)
                                                    .eq(SysUser::getStatus, 1)
        );
                          
        // 用户名不存在直接抛出异常
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        // 校验密码是否匹配
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        return AuthLoginVO.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .avatar(user.getAvatar())
                            .status(user.getStatus())
                            .lastLoginAt(user.getLastLoginAt())
                            .updatedAt(user.getUpdatedAt())
                            .build();
    }
}
