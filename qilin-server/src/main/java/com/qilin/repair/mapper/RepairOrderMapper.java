package com.qilin.repair.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qilin.repair.entity.RepairOrder;

/**
 * 工单主表Mapper
 * RepairOrderMapper
 */
@Mapper 
public interface RepairOrderMapper extends BaseMapper<RepairOrder>{
    /**
     * 维修工接单，更改工单状态为已接单，并设置维修工ID，接单时间
     * @param repairOrder 工单对象，包含id和workerId
     * @return int 受影响的行数
     */
    @Update("""
        UPDATE biz_repair_order
        SET worker_id = #{workerId},
            status = #{status},
            accept_time = #{acceptTime}
        WHERE id = #{id}
        AND status = 'PENDING_DISPATCH'
        AND worker_id IS NULL
        AND deleted = 0
        """)
    int dispatchOrder(RepairOrder repairOrder);

    @Update("""
        UPDATE biz_repair_order
        SET status = 'PROCESSING'
        WHERE id = #{id}
        AND worker_id = #{workerId}
        AND status = 'ACCEPTED'
        AND deleted = 0
        """)
    int startOrder(RepairOrder repairOrder);

    @Update("""
        UPDATE biz_repair_order
        SET status = 'FINISHORDER',
            finish_time = #{finishTime}
        WHERE id = #{id}
        AND worker_id = #{workerId}
        AND status = 'PROCESSING'
        AND deleted = 0
        """)
    int finishOrder(RepairOrder repairOrder);
}
