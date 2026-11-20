package com.qilin.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qilin.module.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper。
 *
 * <p>BaseMapper 已经提供了 selectById / selectPage / updateById 等通用方法，
 * 只有写不清的 SQL 才需要自己写。这里用注解 SQL，
 * 等 SQL 变复杂（多表分页、动态条件）再挪到 resources/mapper/*.xml。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 按用户名查用户（登录用）。
     * 注意手写 SQL 时逻辑删除要自己带上，MyBatis-Plus 只对 Wrapper 生效。
     */
    @Select("""
            SELECT *
              FROM sys_user
             WHERE username = #{username}
               AND deleted = 0
             LIMIT 1
            """)
    SysUser selectByUsername(@Param("username") String username);
}
