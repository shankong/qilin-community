package com.qilin.repair.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

/**
 * 工单主表实体
 */
@Data
@TableName("biz_repair_order")
public class RepairOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 工单号，格式：RO20261124000001
     */
    private String orderNo;

    /**
     * 房屋ID（暂时可为空，待房屋表创建后关联）
     */
    private Long houseId;

    /**
     * 业主ID，关联sys_user
     */
    private Long ownerId;

    /**
     * 维修工ID，关联sys_user
     */
    private Long workerId;

    /**
     * 工单类型：PLUMBING(水电), RENOVATION(装修), APPLIANCE(电器), OTHER(其他)
     */
    private String type;

    /**
     * 状态：PENDING_DISPATCH, DISPATCHED, ACCEPTED, PROCESSING, COMPLETED, EVALUATED, CANCELLED, REJECTED
     */
    private String status;

    /**
     * 紧急程度：LOW(低), MEDIUM(中), HIGH(高)
     */
    private String urgency;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 期望上门时间
     */
    private LocalDateTime expectTime;

    /**
     * 接单时间
     */
    private LocalDateTime acceptTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 评分 1-5
     */
    private Integer score;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除 0=未删除 1=已删除
     */
    private Integer deleted;
}
