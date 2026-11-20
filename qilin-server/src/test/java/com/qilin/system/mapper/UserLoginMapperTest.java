package com.qilin.system.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qilin.system.entity.SysUser;
import org.junit.jupiter.api.Test;

@SpringBootTest 
public class UserLoginMapperTest {
    
    @Autowired 
    private AuthLoginMapper userLoginMapper;

    @Test
    void findByUsername() {
        SysUser user = userLoginMapper.selectOne(
                new QueryWrapper<SysUser>().eq("username", "admin")
        );
        System.out.println(user);
        System.out.println("大家好");
    }
    
}
