package com.qilin.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表 sys_user 的实体。
 *
 * <p>注意：实体里包含 password 字段，但绝不能直接把实体返回给前端——
 * 统一用 VO（如 SysUserVO、UserInfoVO）过滤敏感字段。
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 哈希；初始为空，由 DataInitializer 首次启动时写入 */
    private String password;

    private String realName;

    private String phone;

    private String avatar;

    /** 0 禁用 1 启用 */
    private Integer status;

    private LocalDateTime lastLoginAt;

    private String lastLoginIp;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 逻辑删除：0 正常 1 删除（MyBatis-Plus 自动拼接 deleted = 0） */
    @TableLogic
    private Integer deleted;
}
