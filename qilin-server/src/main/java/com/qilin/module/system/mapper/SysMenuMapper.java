package com.qilin.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qilin.module.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单/权限 Mapper。
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查某个用户的所有权限标识（按钮级）。
     *
     * <p>三表关联：用户 → 角色 → 菜单，只取 perms 非空的项。
     * 结果去重：一个用户可能同时有多个角色，重复权限只保留一条。
     */
    @Select("""
            SELECT DISTINCT m.perms
              FROM sys_menu m
              JOIN sys_role_menu rm ON rm.menu_id = m.id
              JOIN sys_user_role ur  ON ur.role_id = rm.role_id
             WHERE ur.user_id = #{userId}
               AND m.status = 1
               AND m.perms <> ''
            """)
    List<String> selectPermsByUserId(@Param("userId") Long userId);
}
