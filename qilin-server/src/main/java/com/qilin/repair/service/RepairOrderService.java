package com.qilin.repair.service;

import com.qilin.repair.dto.request.CreateRepairOrderRequest;
import com.qilin.repair.entity.RepairOrder;

public interface RepairOrderService {
    // 提交报修
    RepairOrder submitOrder(Long ownerId, CreateRepairOrderRequest request);
}
