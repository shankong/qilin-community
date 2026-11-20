package com.qilin.system.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data 
@TableName("sys_user")
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String avatar; // 头像
    private Integer status;

    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private String remark; // 备注

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer deleted; // 是否删除
}
