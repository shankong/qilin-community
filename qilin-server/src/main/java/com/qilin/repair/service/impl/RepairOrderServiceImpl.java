package com.qilin.repair.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qilin.repair.dto.request.CreateRepairOrderRequest;
import com.qilin.repair.entity.RepairOrder;
import com.qilin.repair.entity.RepairOrderLog;
import com.qilin.repair.enums.RepairOrderStatus;
import com.qilin.repair.mapper.RepairOrderLogMapper;
import com.qilin.repair.mapper.RepairOrderMapper;
import com.qilin.repair.service.RepairOrderService;
import com.qilin.system.exception.BusinessException;

import jakarta.annotation.Resource;

@Service 
public class RepairOrderServiceImpl implements RepairOrderService {
    @Resource 
    private StringRedisTemplate stringRedisTemplate;
    @Resource 
    private RepairOrderMapper repairOrderMapper;
    @Resource 
    private RepairOrderLogMapper repairOrderLogMapper;

    /**    
     * 提交报修单
     * @param ownerId 用户ID
     * @param request 创建报修单请求对象
     * @return RepairOrder 创建的报修单对象
     */
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
        repairOrder.setStatus(RepairOrderStatus.PENDING_DISPATCH.name());
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
        repairOrderLog.setToStatus(RepairOrderStatus.PENDING_DISPATCH.name()); // 设置为工单的初始状态
        repairOrderLog.setOperatorId(ownerId);
        repairOrderLog.setRemark("工单创建");
        repairOrderLog.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
        repairOrderLogMapper.insert(repairOrderLog);

        // 4. 返回工单对象
        return repairOrder;
    }

    /**
     * 分页查询业主所有的报修单
     * @param ownerId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return PageInfo<RepairOrder> 业主的报修单分页列表
     */
    @Override
    public PageInfo<RepairOrder> getOrdersByOwnerId(Long ownerId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getOwnerId, ownerId)
                .orderByDesc(RepairOrder::getCreatedAt);
        return new PageInfo<>(repairOrderMapper.selectList(wrapper));
    }

    /**
     * 查询报修单详情
     * @param ownerId 当前业主ID
     * @param orderId 报修单ID
     * @return RepairOrder 报修单详情对象
     */
    @Override
    public RepairOrder getOrderDetails(Long ownerId, Long orderId) {
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getId, orderId)
                .eq(RepairOrder::getOwnerId, ownerId);
        return repairOrderMapper.selectOne(wrapper);
    }

    /**
     * 管理员派送订单
     * @param orderId 工单ID
     * @param managerId 管理员ID
     * @param workerId 工人ID
     * @return RepairOrder 派送后的工单对象
     */
    @Override
    @Transactional
    public RepairOrder dispatchOrder(Long orderId, Long managerId, Long workerId) {
        RepairOrder repairOrder = repairOrderMapper.selectById(orderId);
        if (repairOrder == null) {
            throw new BusinessException("工单不存在");
        }
        repairOrder.setWorkerId(workerId);
        repairOrder.setStatus(RepairOrderStatus.DISPATCHED.name());
        repairOrderMapper.dispatchOrder(repairOrder);

        // 保存日志
        RepairOrderLog repairOrderLog = new RepairOrderLog();
        repairOrderLog.setOrderId(orderId);
        repairOrderLog.setFromStatus(RepairOrderStatus.PENDING_DISPATCH.name());
        repairOrderLog.setToStatus(RepairOrderStatus.DISPATCHED.name());
        repairOrderLog.setOperatorId(managerId);
        repairOrderLog.setRemark("管理员派单");
        repairOrderLog.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
        repairOrderLogMapper.insert(repairOrderLog);

        return repairOrder;
    }
}
