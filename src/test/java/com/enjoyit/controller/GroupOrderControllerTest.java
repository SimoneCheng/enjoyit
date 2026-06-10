package com.enjoyit.controller;

import com.enjoyit.domain.GroupOrder;
import com.enjoyit.repository.GroupOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupOrderControllerTest {

    @Mock
    private GroupOrderRepository groupOrderRepository;

    @InjectMocks
    private GroupOrderController groupOrderController;

    @Test
    void testPublishGroupOrder_Success() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("orderInfo", "測試團購名稱");
        request.put("announcement", "這是一則公告");
        request.put("vendorId", "vendor_001");
        request.put("adminPassword", "123456");
        request.put("groupId", "group_A");

        // 模擬 Repository 儲存行為
        when(groupOrderRepository.save(any(GroupOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ResponseEntity<?> response = groupOrderController.publishGroupOrder(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody(), "應回傳產生的 orderId");

        // 驗證是否有呼叫 repository 儲存
        verify(groupOrderRepository, times(1)).save(any(GroupOrder.class));
    }
}