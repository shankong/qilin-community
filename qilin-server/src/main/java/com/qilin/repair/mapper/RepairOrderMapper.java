package com.qilin.repair.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qilin.repair.entity.RepairOrder;

/**
 * 工单主表Mapper
 * RepairOrderMapper
 */
@Mapper 
public interface RepairOrderMapper extends BaseMapper<RepairOrder>{
    
}
