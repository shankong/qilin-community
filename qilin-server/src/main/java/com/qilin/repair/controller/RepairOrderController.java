package com.qilin.repair.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qilin.repair.dto.request.CreateRepairOrderRequest;
import com.qilin.repair.entity.RepairOrder;
import com.qilin.repair.service.RepairOrderService;
import com.qilin.repair.vo.RepairOrderVO.CreateRepairOrderVO;
import com.qilin.system.common.Result;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class RepairOrderController {
    @Resource 
    private RepairOrderService repairOrderService;

    /**
     * 提交报修单
     * 只有拥有OWNER角色的用户才能提交报修单
     * @param request 报修单请求参数
     * @param authentication 当前用户的认证信息
     * @return 返回报修单的ID、订单号和状态
     */
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/repair-orders")
    public Result<CreateRepairOrderVO> submitRepairOrder(
        @Valid @RequestBody CreateRepairOrderRequest request,
        Authentication authentication) {
        Long ownerId = (Long) authentication.getPrincipal();
        RepairOrder order = repairOrderService.submitOrder(ownerId, request);
        return Result.success(new CreateRepairOrderVO(
            order.getId(), 
            order.getOrderNo(), 
            order.getStatus()
        ));
    }
}
