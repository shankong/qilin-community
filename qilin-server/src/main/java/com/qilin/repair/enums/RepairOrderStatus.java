package com.qilin.repair.enums;

public enum RepairOrderStatus {
    PENDING_DISPATCH("待派单"),
    ACCEPTED("已接单"),
    PROCESSING("处理中"),
    FINISHORDER("维修完成"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    REJECTED("已拒绝");

    private final String label;

    RepairOrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
