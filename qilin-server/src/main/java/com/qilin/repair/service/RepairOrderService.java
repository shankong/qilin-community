package com.qilin.repair.service;

import java.util.List;

import com.qilin.repair.dto.request.CreateRepairOrderRequest;
import com.qilin.repair.entity.RepairOrder;

public interface RepairOrderService {
    // 提交报修
    RepairOrder submitOrder(Long ownerId, CreateRepairOrderRequest request);
    // 查询业主所有的报修单
    List<RepairOrder> getOrdersByOwnerId(Long ownerId);
    // 查询报修单详情
    RepairOrder getOrderDetails(Long ownerId, Long orderId);
}
