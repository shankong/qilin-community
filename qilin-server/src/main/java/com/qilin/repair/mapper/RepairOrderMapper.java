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
     * 派送工单
     * @param repairOrder 工单对象，包含id和workerId
     * @return int 受影响的行数
     */
    @Update("""
        UPDATE biz_repair_order
        SET worker_id = #{workerId},
            status = #{status}
        WHERE id = #{id}
        AND status = 'PENDING_DISPATCH'
        AND deleted = 0
        """)
    int dispatchOrder(RepairOrder repairOrder);
}
