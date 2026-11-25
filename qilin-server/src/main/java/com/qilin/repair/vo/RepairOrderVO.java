package com.qilin.repair.vo;

public class RepairOrderVO {
    public record CreateRepairOrderVO(Long id, String orderNo, String status) {}
}
