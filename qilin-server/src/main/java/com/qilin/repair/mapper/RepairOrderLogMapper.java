package com.qilin.repair.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qilin.repair.entity.RepairOrderLog;

/**
 * 工单流转日志表Mapper
 * RepairOrderLogMapper
 */
@Mapper 
public interface RepairOrderLogMapper extends BaseMapper<RepairOrderLog>{
    
}
