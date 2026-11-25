package com.qilin.repair.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qilin.repair.entity.RepairWorker;

/**
 * 维修工档案表Mapper
 * RepairWorkerMapper
 */
@Mapper 
public interface RepairWorkerMapper extends BaseMapper<RepairWorker>{
    
}
