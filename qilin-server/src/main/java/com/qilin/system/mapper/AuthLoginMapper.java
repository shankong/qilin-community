package com.qilin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.qilin.system.entity.SysUser;

@Mapper
public interface  AuthLoginMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户ID查询角色编码列表
     * @param userId
     * @return 角色编码列表
     */
    @Select("""
        select r.role_code from sys_role r 
        inner join sys_user_role ur on r.id = ur.role_id
        where ur.user_id = #{userId}
          and r.status = 1
          and r.deleted = 0
    """)
    List<String> selectRoleCodesByUserId(Long userId);
}
