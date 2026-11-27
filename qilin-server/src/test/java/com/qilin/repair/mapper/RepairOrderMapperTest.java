package com.qilin.repair.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qilin.repair.entity.RepairOrder;
import com.qilin.repair.entity.RepairOrderLog;
import com.qilin.repair.service.RepairOrderService;
import com.qilin.system.exception.BusinessException;

import jakarta.annotation.Resource;

@SpringBootTest 
public class RepairOrderMapperTest {
    
    @Resource 
    private RepairOrderMapper repairOrderMapper;

    @Resource
    private RepairOrderService orders;

    @Resource
    private RepairOrderLogMapper logs;

    @Test
    @Transactional
    void pendingOrderCanBeAcceptedStartedAndFinishedByItsWorker() {
        RepairOrder order = insertPendingOrder(0, LocalDateTime.of(2099, 1, 1, 0, 0));
        insertPendingOrder(1, LocalDateTime.of(2099, 1, 2, 0, 0));
        Long id = order.getId();
        Long workerId = 100L;

        var page = orders.getPendingOrders(1, 1);
        assertEquals(1, page.getList().size());
        assertEquals(id.toString(), page.getList().get(0).id());
        assertEquals(1, page.getPageSize());

        assertEquals("ACCEPTED", orders.acceptOrder(id, workerId).getStatus());
        assertEquals(409, assertThrows(BusinessException.class,
                () -> orders.acceptOrder(id, 101L)).getCode());
        assertEquals(409, assertThrows(BusinessException.class,
                () -> orders.finishOrder(id, workerId)).getCode());
        LocalDateTime acceptedAt = repairOrderMapper.selectById(id).getAcceptTime();

        assertEquals("PROCESSING", orders.startOrder(id, workerId).getStatus());
        assertEquals("FINISHORDER", orders.finishOrder(id, workerId).getStatus());

        RepairOrder finished = repairOrderMapper.selectById(id);
        assertEquals("FINISHORDER", finished.getStatus());
        assertEquals(workerId, finished.getWorkerId());
        assertEquals(acceptedAt, finished.getAcceptTime());
        assertNotNull(finished.getFinishTime());
        List<RepairOrderLog> history = logs.selectList(new QueryWrapper<RepairOrderLog>()
                .eq("order_id", id).orderByAsc("id"));
        assertEquals(List.of("ACCEPTED", "PROCESSING", "FINISHORDER"),
                history.stream().map(RepairOrderLog::getToStatus).toList());
    }

    private RepairOrder insertPendingOrder(int deleted, LocalDateTime createdAt) {
        RepairOrder order = new RepairOrder();
        order.setOrderNo("TEST-" + UUID.randomUUID().toString().substring(0, 23));
        order.setOwnerId(1L);
        order.setType("PLUMBING");
        order.setStatus("PENDING_DISPATCH");
        order.setDescription("工单流转测试");
        order.setCreatedAt(createdAt);
        order.setDeleted(deleted);
        repairOrderMapper.insert(order);
        return order;
    }

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
