package com.qilin.repair.mapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qilin.repair.entity.RepairOrder;

import jakarta.annotation.Resource;

@SpringBootTest 
public class RepairOrderMapperTest {
    
    @Resource 
    private RepairOrderMapper repairOrderMapper;

    @Test 
    void testSelectById(){
        RepairOrder order = repairOrderMapper.selectById(1L);
        System.out.println("工单号: " + order.getOrderNo());
        assertNotNull(order);  // 断言工单对象不为空
    }

    @Test
    void testSelectByStatus() {
        // 查询所有待派单工单
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PENDING_DISPATCH");
        List<RepairOrder> orders = repairOrderMapper.selectList(wrapper);
        System.out.println("待派单工单数: " + orders.size());
    }
    @Test
    void testEncoding() {
        System.out.println("file.encoding: " + System.getProperty("file.encoding"));
        System.out.println("sun.stdout.encoding: " + System.getProperty("sun.stdout.encoding"));
        System.out.println("sun.stderr.encoding: " + System.getProperty("sun.stderr.encoding"));
        System.out.println("控制台测试：你好世界 🚀");
    }
}