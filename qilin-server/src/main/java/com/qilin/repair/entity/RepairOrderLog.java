package com.qilin.repair.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 工单流转日志表实体
 */
@Data
@TableName("biz_repair_order_log")
public class RepairOrderLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 工单ID
     */
    private Long orderId;

    /**
     * 原状态
     */
    private String fromStatus;

    /**
     * 新状态
     */
    private String toStatus;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
