package com.qilin.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qilin.module.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /** 查某个用户拥有的角色编码（登录时算权限用） */
    @Select("""
            SELECT r.role_code
              FROM sys_role r
              JOIN sys_user_role ur ON ur.role_id = r.id
             WHERE ur.user_id = #{userId}
               AND r.deleted = 0
               AND r.status = 1
            """)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
