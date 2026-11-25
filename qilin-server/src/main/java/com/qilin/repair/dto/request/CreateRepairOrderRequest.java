package com.qilin.repair.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data 
public class CreateRepairOrderRequest {
    @NotBlank(message = "工单类型不能为空")
    private String type;
    
    @NotBlank(message = "问题描述不能为空")
    private String description;
    
    private String urgency = "MEDIUM"; // 默认中等紧急
    
    private LocalDateTime expectTime; // 期望上门时间
}
