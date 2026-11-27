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
import com.qilin.repair.vo.DispatchRepairOrderVO.CreateRepairOrderVO;
import com.qilin.repair.vo.DispatchRepairOrderVO.DispatchRepairOrderResultVO;
import com.qilin.repair.vo.DispatchRepairOrderVO.PendingRepairOrderVO;
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
     * 查询待接单报修单列表
     * 只有拥有WORKER角色的用户才能查询待接单报修单
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 返回待接单报修单分页列表
     */
    @PreAuthorize("hasRole('WORKER')")
    @GetMapping("/pool")
    public Result<PageInfo<PendingRepairOrderVO>> getPendingOrders(
        @RequestParam(name = "pageNum", defaultValue = "1") int pageNum,
        @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        return Result.success(repairOrderService.getPendingOrders(pageNum, pageSize));
    }

    /**
     * 维修工接单
     * 只有拥有WORKER角色的用户才能接单
     * @param id 报修单ID
     * @param authentication 当前用户的认证信息
     * @return 返回接单后的报修单对象
     */
    @PreAuthorize("hasRole('WORKER')")
    @PutMapping("/{id}/accept")
    public Result<DispatchRepairOrderResultVO> acceptOrder(
        @PathVariable Long id,
        Authentication authentication) {
        Long workerId = (Long) authentication.getPrincipal();
        RepairOrder order = repairOrderService.acceptOrder(id, workerId);
        return Result.success(new DispatchRepairOrderResultVO(
            order.getId(),
            order.getOrderNo(),
            order.getStatus(),
            order.getWorkerId()
        ));
    }

    /**
     * 维修工开始维修
     * 只有拥有WORKER角色的用户才能开始维修
     * @param id 报修单ID
     * @param authentication 当前用户的认证信息
     * @return 返回开始维修后的报修单对象
     */
    @PreAuthorize("hasRole('WORKER')")
    @PutMapping("/{id}/start")
    public Result<DispatchRepairOrderResultVO> startOrder(
        @PathVariable Long id,
        Authentication authentication) {
        Long workerId = (Long) authentication.getPrincipal();
        RepairOrder order = repairOrderService.startOrder(id, workerId);
        return Result.success(new DispatchRepairOrderResultVO(
            order.getId(),
            order.getOrderNo(),
            order.getStatus(),
            order.getWorkerId()
        ));
    }

    /**
     * 维修工完成处理
     * 只有拥有WORKER角色的用户才能完成处理
     * @param id 报修单ID
     * @param authentication 当前用户的认证信息
     * @return 返回处理后的报修单对象
     */
    @PreAuthorize("hasRole('WORKER')")
    @PutMapping("/{id}/finish")
    public Result<DispatchRepairOrderResultVO> finishOrder(
        @PathVariable Long id,
        Authentication authentication) {
        Long workerId = (Long) authentication.getPrincipal();
        RepairOrder order = repairOrderService.finishOrder(id, workerId);
        return Result.success(new DispatchRepairOrderResultVO(
            order.getId(),
            order.getOrderNo(),
            order.getStatus(),
            order.getWorkerId()
        ));
    }
}
