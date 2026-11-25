package com.qilin.repair.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 维修工档案表实体
 */
@Data
@TableName("biz_repair_worker")
public class RepairWorker implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 关联sys_user的用户ID
     */
    private Long userId;

    /**
     * 技能标签，JSON数组格式，如：["水电","装修","电器"]
     */
    private String skills;

    /**
     * 当前工作量（未完成工单数）
     */
    private Integer workload;

    /**
     * 状态：ACTIVE(在职), INACTIVE(离职)
     */
    private String status;

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
