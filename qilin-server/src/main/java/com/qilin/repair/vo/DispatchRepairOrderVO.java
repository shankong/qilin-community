package com.qilin.repair.vo;

import java.time.LocalDateTime;

public class DispatchRepairOrderVO {
    /**
     * 创建报修单VO
     * @param id 报修单ID
     * @param orderNo 报修单编号
     * @param status 报修单状态
     */
    public record CreateRepairOrderVO(Long id, String orderNo, String status) {}

    /**
     * 派单结果VO
     * @param id 报修单ID
     * @param orderNo 报修单编号
     * @param status 报修单状态
     * @param workerId 维修工ID
     */
    public record DispatchRepairOrderResultVO(Long id, String orderNo, String status, Long workerId) {}

    /**
     * 待接单报修单。
     * @param id 工单 ID
     * @param orderNo 工单编号
     * @param type 报修类型
     * @param urgency 紧急程度
     * @param expectTime 期望维修时间
     */
    public record PendingRepairOrderVO(
            String id,
            String orderNo,
            String type,
            String urgency,
            LocalDateTime expectTime) {}
}
