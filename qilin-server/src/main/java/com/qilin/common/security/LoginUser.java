package com.qilin.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 登录用户，登录成功后放进 Session。
 *
 * <p>为什么要序列化：现在存在内存里无所谓，但切到 Redis Session 后
 * Session 内容会被序列化，不实现 {@link Serializable} 会直接报错。
 *
 * <p>为什么把 roles/perms 也放进 Session：
 * 每次请求都查库会多 2 次查询；登录时算一次，之后从 Session 取。
 * 代价是管理员改了权限需要用户重新登录才生效——P0 阶段可以接受，
 * 后面可以用 Redis 存"权限版本号"来解决。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Session 中的属性名 */
    public static final String SESSION_KEY = "QILIN_LOGIN_USER";

    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    /** 角色编码：ADMIN / MANAGER / WORKER / OWNER */
    private List<String> roles;
    /** 权限标识：system:user:list、biz:order:dispatch ... */
    private List<String> perms;
    private String loginIp;
    private LocalDateTime loginTime;

    /** 是否拥有其中任意一个角色 */
    public boolean hasAnyRole(String... roleCodes) {
        if (roleCodes == null || roleCodes.length == 0) {
            return true;
        }
        return roles != null && Arrays.stream(roleCodes).anyMatch(roles::contains);
    }

    /** 是否拥有其中任意一个权限 */
    public boolean hasAnyPerm(String... permCodes) {
        if (permCodes == null || permCodes.length == 0) {
            return true;
        }
        return perms != null && Arrays.stream(permCodes).anyMatch(perms::contains);
    }
}
