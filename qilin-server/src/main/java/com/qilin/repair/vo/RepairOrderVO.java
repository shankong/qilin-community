package com.qilin.repair.vo;

public class RepairOrderVO {
    public record CreateRepairOrderVO(Long id, String orderNo, String status) {}

    public record DispatchRepairOrderVO(Long id, String orderNo, String status, Long workerId) {}
}
