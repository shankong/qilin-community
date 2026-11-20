package com.qilin.module.auth.dto;

import com.qilin.common.security.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录成功后返回给前端的用户信息。
 *
 * <p>perms 直接给前端做按钮级控制（v-permission 指令）；
 * roles 用来决定进哪个端（业主/维修工/管理员）。
 *
 * <p>注意这里绝不包含 password。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {

    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private List<String> roles;
    private List<String> perms;
    private LocalDateTime loginTime;

    public static UserInfoVO from(LoginUser loginUser) {
        return UserInfoVO.builder()
                .userId(loginUser.getUserId())
                .username(loginUser.getUsername())
                .realName(loginUser.getRealName())
                .avatar(loginUser.getAvatar())
                .roles(loginUser.getRoles())
                .perms(loginUser.getPerms())
                .loginTime(loginUser.getLoginTime())
                .build();
    }
}
