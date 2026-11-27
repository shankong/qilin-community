package com.qilin.repair.service;

import com.github.pagehelper.PageInfo;
import com.qilin.repair.dto.request.CreateRepairOrderRequest;
import com.qilin.repair.entity.RepairOrder;
import com.qilin.repair.vo.DispatchRepairOrderVO.PendingRepairOrderVO;

public interface RepairOrderService {
    // 提交报修
    RepairOrder submitOrder(Long ownerId, CreateRepairOrderRequest request);
    // 查询业主所有的报修单
    PageInfo<RepairOrder> getOrdersByOwnerId(Long ownerId, Integer pageNum, Integer pageSize);
    // 查询报修单详情
    RepairOrder getOrderDetails(Long ownerId, Long orderId);
    PageInfo<PendingRepairOrderVO> getPendingOrders(int pageNum, int pageSize);
    // 维修工接单
    RepairOrder acceptOrder(Long orderId, Long workerId);
    RepairOrder startOrder(Long orderId, Long workerId);
    // 维修工完成处理
    RepairOrder finishOrder(Long orderId, Long workerId);
}
