package com.qilin.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.qilin.system.entity.SysUser;

@Mapper
public interface  AuthLoginMapper extends BaseMapper<SysUser> {
    
}
