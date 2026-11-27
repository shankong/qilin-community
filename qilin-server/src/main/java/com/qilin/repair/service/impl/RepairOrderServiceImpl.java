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
import com.qilin.repair.vo.DispatchRepairOrderVO.PendingRepairOrderVO;
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

    @Override
    public PageInfo<PendingRepairOrderVO> getPendingOrders(int pageNum, int pageSize) {
        if (pageNum < 1 || pageSize < 1 || pageSize > 100) {
            throw new BusinessException("页码至少为 1，每页条数须在 1 到 100 之间");
        }
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<RepairOrder>()
                .select(RepairOrder::getId, RepairOrder::getOrderNo, RepairOrder::getType,
                        RepairOrder::getUrgency, RepairOrder::getExpectTime)
                .eq(RepairOrder::getStatus, RepairOrderStatus.PENDING_DISPATCH.name())
                .isNull(RepairOrder::getWorkerId)
                .eq(RepairOrder::getDeleted, 0)
                .orderByDesc(RepairOrder::getCreatedAt, RepairOrder::getId);
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(repairOrderMapper.selectList(wrapper)).convert(order ->
                new PendingRepairOrderVO(order.getId().toString(), order.getOrderNo(), order.getType(),
                        order.getUrgency(), order.getExpectTime()));
    }

    /**
     * 维修工接单
     * @param orderId 报修单ID
     * @param workerId 维修工ID
     * @return RepairOrder 接单后的报修单对象
     */
    @Override
    @Transactional
    public RepairOrder acceptOrder(Long orderId, Long workerId) {
        RepairOrder repairOrder = requireOrder(orderId);
        repairOrder.setWorkerId(workerId);
        repairOrder.setStatus(RepairOrderStatus.ACCEPTED.name());
        repairOrder.setAcceptTime(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
        if (repairOrderMapper.dispatchOrder(repairOrder) != 1) {
            throw new BusinessException(409, "工单已被接取或已退出待接单列表");
        }
        logTransition(repairOrder, RepairOrderStatus.PENDING_DISPATCH, workerId, "维修工接单");
        return repairOrder;
    }

    /**
     * 维修工开始维修
     * @param orderId 报修单ID
     * @param workerId 维修工ID
     * @return RepairOrder 开始维修后的报修单对象
     */
    @Override
    @Transactional
    public RepairOrder startOrder(Long orderId, Long workerId) {
        RepairOrder repairOrder = requireAssignedOrder(orderId, workerId);
        if (repairOrderMapper.startOrder(repairOrder) != 1) {
            throw new BusinessException(409, "只有已接单的工单可以开始维修");
        }
        repairOrder.setStatus(RepairOrderStatus.PROCESSING.name());
        logTransition(repairOrder, RepairOrderStatus.ACCEPTED, workerId, "开始维修");
        return repairOrder;
    }

    /**
     * 维修工完成处理
     * @param orderId 报修单ID
     * @param workerId 维修工ID
     * @return RepairOrder 完成处理后的报修单对象
     */
    @Override
    @Transactional
    public RepairOrder finishOrder(Long orderId, Long workerId) {
        RepairOrder repairOrder = requireAssignedOrder(orderId, workerId);
        repairOrder.setFinishTime(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
        if (repairOrderMapper.finishOrder(repairOrder) != 1) {
            throw new BusinessException(409, "只有维修中的工单可以报完工");
        }
        repairOrder.setStatus(RepairOrderStatus.FINISHORDER.name());
        logTransition(repairOrder, RepairOrderStatus.PROCESSING, workerId, "维修已完成");
        return repairOrder;
    }

    /**
     * 根据报修单ID查询报修单，若不存在则抛出异常
     * @param orderId 报修单ID
     * @return RepairOrder 报修单对象
     */
    private RepairOrder requireOrder(Long orderId) {
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if (order == null || Integer.valueOf(1).equals(order.getDeleted())) {
            throw new BusinessException(404, "报修单不存在");
        }
        return order;
    }

    /**
     * 根据报修单ID和维修工ID查询报修单，若不存在或维修工不匹配则抛出异常
     * @param orderId 报修单ID
     * @param workerId 维修工ID
     * @return RepairOrder 报修单对象
     */
    private RepairOrder requireAssignedOrder(Long orderId, Long workerId) {
        RepairOrder order = requireOrder(orderId);
        if (!workerId.equals(order.getWorkerId())) {
            throw new BusinessException(403, "只有承接该工单的维修工可以操作");
        }
        return order;
    }

    /**
     * 记录工单状态变更日志
     * @param repairOrder 报修单对象
     * @param fromStatus 变更前的状态
     * @param workerId 操作的维修工ID
     * @param remark 日志备注信息
     */
    private void logTransition(RepairOrder repairOrder, RepairOrderStatus fromStatus,
            Long workerId, String remark) {
        RepairOrderLog repairOrderLog = new RepairOrderLog();
        repairOrderLog.setOrderId(repairOrder.getId());
        repairOrderLog.setFromStatus(fromStatus.name());
        repairOrderLog.setToStatus(repairOrder.getStatus());
        repairOrderLog.setOperatorId(workerId);
        repairOrderLog.setRemark(remark);
        repairOrderLog.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Shanghai")));
        repairOrderLogMapper.insert(repairOrderLog);
    }
}
