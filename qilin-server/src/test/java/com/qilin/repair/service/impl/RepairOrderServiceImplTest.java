package com.qilin.repair.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.qilin.repair.entity.RepairOrder;
import com.qilin.repair.mapper.RepairOrderLogMapper;
import com.qilin.repair.mapper.RepairOrderMapper;
import com.qilin.system.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class RepairOrderServiceImplTest {
    @Mock
    private RepairOrderMapper orders;
    @Mock
    private RepairOrderLogMapper logs;
    @InjectMocks
    private RepairOrderServiceImpl service;

    @Test
    void losingAnAcceptanceRaceReturnsConflictWithoutWritingALog() {
        when(orders.selectById(1L)).thenReturn(order("PENDING_DISPATCH", null));
        when(orders.dispatchOrder(any(RepairOrder.class))).thenReturn(0);

        assertEquals(409, assertThrows(BusinessException.class,
                () -> service.acceptOrder(1L, 10L)).getCode());
        verifyNoInteractions(logs);
    }

    @Test
    void anotherWorkerCannotFinishAnAssignedOrder() {
        when(orders.selectById(1L)).thenReturn(order("PROCESSING", 20L));

        assertEquals(403, assertThrows(BusinessException.class,
                () -> service.finishOrder(1L, 10L)).getCode());
        verifyNoInteractions(logs);
    }

    @Test
    void missingOrderReturnsNotFoundForBothActions() {
        assertEquals(404, assertThrows(BusinessException.class,
                () -> service.acceptOrder(1L, 10L)).getCode());
        assertEquals(404, assertThrows(BusinessException.class,
                () -> service.finishOrder(1L, 10L)).getCode());
        verifyNoInteractions(logs);
    }

    private RepairOrder order(String status, Long workerId) {
        RepairOrder order = new RepairOrder();
        order.setId(1L);
        order.setStatus(status);
        order.setWorkerId(workerId);
        return order;
    }
}
