package com.qilin.repair.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qilin.repair.dto.request.CreateRepairOrderRequest;
import com.qilin.repair.entity.RepairOrder;
import com.qilin.repair.entity.RepairOrderLog;
import com.qilin.repair.mapper.RepairOrderLogMapper;
import com.qilin.repair.mapper.RepairOrderMapper;
import com.qilin.repair.service.RepairOrderService;

import jakarta.annotation.Resource;

@Service 
public class RepairOrderServiceImpl implements RepairOrderService {
    @Resource 
    private StringRedisTemplate stringRedisTemplate;
    @Resource 
    private RepairOrderMapper repairOrderMapper;
    @Resource 
    private RepairOrderLogMapper repairOrderLogMapper;

    @Override
    @Transactional 
    public RepairOrder submitOrder(Long ownerId, CreateRepairOrderRequest request) {
        // 1. 组装工单
        RepairOrder repairOrder = new RepairOrder();
        String date = LocalDate.now(ZoneId.of("Asia/Shanghai"))
        // (DateTimeFormatter.BASIC_ISO_DATE)把 2026-11-24 格式化为 20261124
                .format(DateTimeFormatter.BASIC_ISO_DATE);  
        // stringRedisTemplate springboot提供的操作redis的工具类
        // opsForValue()操作字符串类型，increment()自增
        Long sequence = stringRedisTemplate.opsForValue()
                .increment("repair_order_seq:" + date);
        // Locale.ROOT 让编号的格式独立于服务器的语言环境
        String orderNo = String.format("RO%s%06d", date, sequence);
        repairOrder.setOrderNo(orderNo);
        repairOrder.setOwnerId(ownerId);
        repairOrder.setStatus("PENDING_DISPATCH");
        repairOrder.setType(request.getType());
        repairOrder.setDescription(request.getDescription());
        repairOrder.setUrgency(request.getUrgency());
        repairOrder.setExpectTime(request.getExpectTime());

        // 2. 将工单保存到数据库中
        repairOrderMapper.insert(repairOrder);

        //3. 保存日志
        RepairOrderLog repairOrderLog = new RepairOrderLog();
        repairOrderLog.setOrderId(repairOrder.getId());
        repairOrderLog.setFromStatus(null);
        repairOrderLog.setToStatus("PENDING_DISPATCH"); // 设置为工单的初始状态
        repairOrderLog.setOperatorId(ownerId);
        repairOrderLog.setRemark("工单创建");
        repairOrderLog.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
        repairOrderLogMapper.insert(repairOrderLog);

        // 4. 返回工单对象
        return repairOrder;
    }
}
