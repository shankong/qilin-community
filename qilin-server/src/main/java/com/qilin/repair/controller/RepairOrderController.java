package com.qilin.repair.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.qilin.repair.dto.request.CreateRepairOrderRequest;
import com.qilin.repair.entity.RepairOrder;
import com.qilin.repair.service.RepairOrderService;
import com.qilin.repair.vo.RepairOrderVO.CreateRepairOrderVO;
import com.qilin.repair.vo.RepairOrderVO.DispatchRepairOrderVO;
import com.qilin.system.common.Result;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/repair-orders")
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
    @PostMapping("/")
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

    /**
     * 查询业主所有的报修单
     * 只有拥有OWNER角色的用户才能查询自己的报修单
     * @param authentication 当前用户的认证信息
     * @return 返回业主的报修单列表
     */
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping
    public Result<PageInfo<RepairOrder>> getRepairOrders(
        Authentication authentication,
        @RequestParam(name = "pageNum", defaultValue = "1") int pageNum,
        @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        Long ownerId = (Long) authentication.getPrincipal();
        PageInfo<RepairOrder> orders = repairOrderService.getOrdersByOwnerId(ownerId, pageNum, pageSize);
        return Result.success(orders);
    }

    /**
     * 查询报修单详情
     * 只有拥有OWNER角色的用户才能查询自己的报修单详情
     * @param id 报修单ID
     * @param authentication 当前用户的认证信息
     * @return 返回报修单详情对象
     */
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/{id}")
    public Result<RepairOrder> getRepairOrderDetails(
        @PathVariable Long id,
        Authentication authentication) {
        Long ownerId = (Long) authentication.getPrincipal();
        RepairOrder order = repairOrderService.getOrderDetails(ownerId, id);
        return Result.success(order);
    }

    /**
     * 管理员派送订单
     * 只有拥有MANAGER角色的用户才能派送订单
     * @param orderId 报修单ID
     * @param workerId 维修工ID
     * @param authentication 当前用户的认证信息
     * @return 返回派送后的报修单对象
     */
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}/dispatch")
    public Result<DispatchRepairOrderVO> dispatchRepairOrder(
        @PathVariable("id") Long orderId,
        @RequestParam Long workerId,
        Authentication authentication) {
        Long managerId = (Long) authentication.getPrincipal();
        RepairOrder order = repairOrderService.dispatchOrder(orderId, managerId, workerId);
        return Result.success(new DispatchRepairOrderVO(
            order.getId(),
            order.getOrderNo(),
            order.getStatus(),
            order.getWorkerId()
        ));
    }
}
