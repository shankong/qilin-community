package com.qilin.module.system.vo;

import com.qilin.module.system.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户列表展示对象。
 *
 * <p>为什么不直接返回 SysUser：实体里有 password 哈希，
 * 一旦序列化出去就是安全事故。VO 的职责就是“只给前端该看的字段”。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String avatar;
    /** 0 禁用 1 启用 */
    private Integer status;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime createdAt;

    public static SysUserVO from(SysUser user) {
        return SysUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
